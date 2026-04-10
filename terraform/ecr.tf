resource "aws_ecr_repository" "app" {
  name         = "bookmyshow-app"
  force_delete = true
}