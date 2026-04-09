provider "aws" {
  region = "us-east-1"
}

resource "aws_ecr_repository" "app" {
  name = "bookmyshow-app"

  image_scanning_configuration {
    scan_on_push = true
  }
}