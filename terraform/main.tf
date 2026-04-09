terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

resource "aws_ecr_repository" "app" {
  name = "bookmyshow-app"

  image_scanning_configuration {
    scan_on_push = true
  }
}


# VPC
resource "aws_vpc" "main" {
  cidr_block = "10.0.0.0/16"

  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "bookmyseat-vpc"
  }
}
# Subnet 1
resource "aws_subnet" "subnet1" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.1.0/24"
  availability_zone = "us-east-1a"
  tags = {
    Name = "bookmyseat-subnet-1"
  }
}

# Subnet 2
resource "aws_subnet" "subnet2" {
  vpc_id            = aws_vpc.main.id
  cidr_block        = "10.0.2.0/24"
  availability_zone = "us-east-1b"
  
  tags = {
    Name = "bookmyseat-subnet-1"
  }
}

# Internet Gateway
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "bookmyseat-igw"
  }
}

# Route Table
resource "aws_route_table" "rt" {
  vpc_id = aws_vpc.main.id
  tags = {
    Name = "bookmyseat-rt"
  }
}

# Route to Internet
resource "aws_route" "internet_access" {
  route_table_id         = aws_route_table.rt.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.igw.id
}

# Associate subnets with route table
resource "aws_route_table_association" "a1" {
  subnet_id      = aws_subnet.subnet1.id
  route_table_id = aws_route_table.rt.id
}

resource "aws_route_table_association" "a2" {
  subnet_id      = aws_subnet.subnet2.id
  route_table_id = aws_route_table.rt.id
}

resource "aws_security_group" "alb_sg" {
  name   = "bookmyseat-alb-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "bookmyseat-alb-sg"
  }
}

resource "aws_lb" "alb" {
  name               = "bookmyseat-alb"
  internal           = false
  load_balancer_type = "application"
  subnets            = [
    aws_subnet.subnet1.id,
    aws_subnet.subnet2.id
  ]
  security_groups    = [aws_security_group.alb_sg.id]

  tags = {
    Name = "bookmyseat-alb"
  }
}

resource "aws_lb_target_group" "tg" {
  name     = "bookmyseat-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = aws_vpc.main.id

  target_type = "ip"

  health_check {
    path = "/actuator/health"
    port = "8080"
  }

  tags = {
    Name = "bookmyseat-tg"
  }
}

resource "aws_lb_listener" "listener" {
  load_balancer_arn = aws_lb.alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.tg.arn
  }
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name = "bookmyseat-ecs-task-exec-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
      Action = "sts:AssumeRole"
    }]
  })

  tags = {
    Name = "bookmyseat-ecs-task-exec-role"
  }
}

resource "aws_iam_role_policy_attachment" "ecs_task_exec_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}


resource "aws_ecs_cluster" "cluster" {
  name = "bookmyseat-cluster"
}

resource "aws_ecs_task_definition" "task" {
  family                   = "bookmyseat-task"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  container_definitions = jsonencode([
    {
      name  = "bookmyseat"
      image = "366707332695.dkr.ecr.us-east-1.amazonaws.com/bookmyshow-app:latest"

      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = "/ecs/bookmyseat"
          awslogs-region        = "us-east-1"
          awslogs-stream-prefix = "ecs"
        }
      }

      secrets = [
        {
          name      = "SPRING_DATASOURCE_USERNAME"
          valueFrom = "${aws_secretsmanager_secret.db_secret.arn}:username::"
        },
        {
          name      = "SPRING_DATASOURCE_PASSWORD"
          valueFrom = "${aws_secretsmanager_secret.db_secret.arn}:password::"
        },
        {
          name      = "SPRING_DATASOURCE_URL"
          valueFrom = "${aws_secretsmanager_secret.db_secret.arn}:url::"
        }
      ]
    }
  ])
}

resource "aws_ecs_service" "service" {
  name            = "bookmyseat-service"
  cluster         = aws_ecs_cluster.cluster.id
  task_definition = aws_ecs_task_definition.task.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets = [
      aws_subnet.subnet1.id,
      aws_subnet.subnet2.id
    ]
    assign_public_ip = true
    security_groups = [aws_security_group.ecs_sg.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.tg.arn
    container_name   = "bookmyseat"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.listener]
}

resource "aws_db_subnet_group" "db_subnet" {
  name = "bookmyseat-db-subnet"

  subnet_ids = [
    aws_subnet.subnet1.id,
    aws_subnet.subnet2.id
  ]

  tags = {
    Name = "bookmyseat-db-subnet"
  }
}

resource "aws_security_group" "db_sg" {
  name   = "bookmyseat-db-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    security_groups = [aws_security_group.ecs_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "bookmyseat-db-sg"
  }
}


resource "aws_db_instance" "postgres" {
  identifier         = "bookmyseat-db"
  engine             = "postgres"
  engine_version     = "15"
  instance_class     = "db.t3.micro"

  allocated_storage  = 20

  username = "postgres"
  password = "postgres123"

  db_subnet_group_name   = aws_db_subnet_group.db_subnet.name
  vpc_security_group_ids = [aws_security_group.db_sg.id]

  skip_final_snapshot = true

  publicly_accessible = true

  tags = {
    Name = "bookmyseat-db"
  }
}



resource "aws_security_group" "ecs_sg" {
  name   = "bookmyseat-ecs-sg"
  vpc_id = aws_vpc.main.id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "bookmyseat-ecs-sg"
  }
}

resource "aws_cloudwatch_log_group" "ecs_logs" {
  name = "/ecs/bookmyseat"
}


resource "aws_secretsmanager_secret" "db_secret" {
  name = "bookmyseat-db-secret"
}

resource "aws_iam_role_policy" "ecs_secrets_policy" {
  name = "ecs-secrets-policy"
  role = aws_iam_role.ecs_task_execution_role.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = aws_secretsmanager_secret.db_secret.arn
      }
    ]
  })
}

