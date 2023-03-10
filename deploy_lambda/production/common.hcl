inputs = {
  aws_region      = "ap-southeast-1"
  s3_state_bucket = "mobius-gcc-prod-terraform"
  dynamodb_state  = "mobius-gcc-prod-terraform"

  vpc_state_key     = "vpc"
  ecr_state_key     = "ecr"
  route53_state_key = "route53"
  acm_state_key     = "acm"
  ecs_state_key     = "ecs"
  elb_state_key     = "elb"
  iam_state_key     = "iam"
  s3_state_key      = "s3"
  sqs_state_key     = "sqs"

  env                = "production"

  tags = {
    Terraform   = true
    Environment = "production"
  }
}
