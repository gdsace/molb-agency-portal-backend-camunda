include {
  path = find_in_parent_folders()
}

locals {
  git_path = trimspace(run_cmd("git", "rev-parse", "--show-toplevel"))
  is_use_local = tobool(get_env("LOCAL", false))
  common_inputs = read_terragrunt_config(find_in_parent_folders("common.hcl")).inputs

  local_source  = "${local.git_path}/../molb-gcc-infra//modules/lambda"
  remote_source = "git::git@github.com:gdsace/molb-gcc-infra.git//modules/lambda?ref=${get_env("DEPLOY_REF", "master")}"
}

terraform {
  source = local.is_use_local ? local.local_source : local.remote_source

}

inputs = {
  service_name = "ap-backend"
  lambda_functions = {
    staging-ap-start-approval-workflow = {
      description = "Repository: molb-agency-portal-backend"
      architectures = ["x86_64"]
      handler       = "ap_sqs_start_approval_workflow.handler"
      role          = "SQS-Queue-Lambda-Function"
      runtime       = "nodejs16.x"
      source_file   = "${local.git_path}/deploy_lambda/deploymentPackages/ap_sqs_start_approval_workflow.js"
      zip_file      = "${local.git_path}/deploy_lambda/deploymentPackages/ap_sqs_start_approval_workflow.zip"
      environment   = {}
      is_monitoring_enabled = false
      vpc_config = {
        subnet_ids = "app"
      }
    }
    staging-ap-l1t-update-status = {
      description = "Repository: molb-agency-portal-backend"
      architectures = ["x86_64"]
      handler       = "ap_sqs_l1t_update_status.handler"
      role          = "SQS-Queue-Lambda-Function"
      runtime       = "nodejs16.x"
      source_file   = "${local.git_path}/deploy_lambda/deploymentPackages/ap_sqs_l1t_update_status.js"
      zip_file      = "${local.git_path}/deploy_lambda/deploymentPackages/ap_sqs_l1t_update_status.zip"
      environment   = {
        JOB_PASSWORD  = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgHWWR5buqd3w2z7345PBdkHAAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMdD/Eude9W9Zn6JfMAgEQgDvm29hRStoh5zjfnOgyHypYek44//x9uUaFK3kYV/zOoaV+rdaXpU8qCyYat59NxXSYZq/i8pWuxHVkCw=="
        JOB_USERNAME  = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgE0SQaZ1C5CqCppTWWTQNJNAAAAeTB3BgkqhkiG9w0BBwagajBoAgEAMGMGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMaVgm6iaqWeScjylbAgEQgDYGRqRx8xG8mpDTrhjJ/tld6AqJFpmUTMORsw1zX1aBbSAr6+ClggoRRaazcrPrLL+2/8FgHgk="
      }
      is_monitoring_enabled = false
      vpc_config = {
        subnet_ids = "app"
      }
    }
    staging-ap-l1t-update-licence-status-job = {
      description = "Repository: molb-agency-portal-backend"
      architectures = ["x86_64"]
      handler       = "ap_l1t_update_licence_status_job.handler"
      role          = "SQS-Queue-Lambda-Function"
      runtime       = "nodejs16.x"
      source_file   = "${local.git_path}/deploy_lambda/deploymentPackages/ap_l1t_update_licence_status_job.js"
      zip_file      = "${local.git_path}/deploy_lambda/deploymentPackages/ap_l1t_update_licence_status_job.zip"
      environment   = {
        JOB_METHOD    = "POST"
        JOB_PASSWORD  = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgHWWR5buqd3w2z7345PBdkHAAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMdD/Eude9W9Zn6JfMAgEQgDvm29hRStoh5zjfnOgyHypYek44//x9uUaFK3kYV/zOoaV+rdaXpU8qCyYat59NxXSYZq/i8pWuxHVkCw=="
        JOB_URL       = "https://api.agencyportal.gobusiness.io/job/cron/update-licence-status"
        JOB_USERNAME  = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgE0SQaZ1C5CqCppTWWTQNJNAAAAeTB3BgkqhkiG9w0BBwagajBoAgEAMGMGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMaVgm6iaqWeScjylbAgEQgDYGRqRx8xG8mpDTrhjJ/tld6AqJFpmUTMORsw1zX1aBbSAr6+ClggoRRaazcrPrLL+2/8FgHgk="
      }
      is_monitoring_enabled = false
      vpc_config = {
        subnet_ids = "app"
      }
    }
  }

  sqs_triggers = {
    approval-workflow = {
      sqs_name             = "staging-ap-start-approval-workflow"
      lambda_function_name = "staging-ap-start-approval-workflow"
      batch_size           = 10
    }
    l1t-update-status = {
      sqs_name             = "staging-ap-l1t-update-status"
      lambda_function_name = "staging-ap-l1t-update-status"
      batch_size           = 1
    }
  }

  eventbridge_cron_triggers = {
    l1t-update_licence_status_job = {
      name                 = "staging-ap-l1t-update-licence-status-job"
      lambda_function_name = "staging-ap-l1t-update-licence-status-job"
      schedule_expression  = "cron(0 16 * * ? *)"
    }
  }
}
