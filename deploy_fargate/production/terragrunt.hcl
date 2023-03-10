remote_state {
  backend = "s3"
  config = {
    bucket         = "mobius-gcc-prod-terraform"
    region         = "ap-southeast-1"
    dynamodb_table = "mobius-gcc-prod-terraform"
    encrypt        = true

    key = "${local.service}"
  }
}
generate "backend" {
  path      = "mobius-gcc-prod-backend.tf"
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
  common_vars  = read_terragrunt_config(find_in_parent_folders("common.hcl")).inputs
  git_path     = trimspace(run_cmd("git", "config", "--get", "remote.origin.url"))
  hyperlink    = split("/", local.git_path)
  service_name = element(local.hyperlink, length(local.hyperlink) - 1)
  service      = trimsuffix(local.service_name, ".git")
}

inputs = merge(
  local.common_vars,
)
