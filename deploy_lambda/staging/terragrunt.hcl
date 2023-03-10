remote_state {
  backend = "s3"
  config = {
    bucket         = "mobius-gcc-stg-terraform"
    region         = "ap-southeast-1"
    dynamodb_table = "mobius-gcc-stg-terraform"
    encrypt        = true

    key = "${local.service}/lambda_state/${path_relative_to_include()}"
  }
}
generate "backend" {
  path      = "mobius-gcc-stg-backend.tf"
  if_exists = "overwrite_terragrunt"
  contents  = <<-EOF
    terraform {
      backend "s3" {}
    }
  EOF
}

generate "caller" {
  path      = "mobius-caller.tf"
  if_exists = "overwrite_terragrunt"
  contents  = <<-EOF
    data "aws_caller_identity" "terragrunt" {}
  EOF
}

locals {
  common_vars = read_terragrunt_config(find_in_parent_folders("common.hcl")).inputs
  git_path = trimspace(run_cmd("git", "config", "--get", "remote.origin.url"))
  hyperlink = split("/", local.git_path)
  service_name = element(local.hyperlink, length(local.hyperlink)-1)
  service = trimsuffix(local.service_name, ".git")
}

inputs = merge(
  local.common_vars,
)
