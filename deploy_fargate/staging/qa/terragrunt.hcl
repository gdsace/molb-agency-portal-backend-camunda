include {
  path = find_in_parent_folders()
}

locals {
  git_path      = trimspace(run_cmd("git", "rev-parse", "--show-toplevel"))
  is_use_local  = tobool(get_env("LOCAL", false))
  common_inputs = read_terragrunt_config(find_in_parent_folders("common.hcl")).inputs

  local_source  = "${local.git_path}/../molb-gcc-infra//modules/ecs_service"
  remote_source = "git::git@github.com:gdsace/molb-gcc-infra.git//modules/ecs_service?ref=${get_env("DEPLOY_REF", "master")}"
}

terraform {
  source = local.is_use_local ? local.local_source : local.remote_source
}

inputs = {
  external             = false
  service_name         = "ap-backend"
  environment_name     = "qa"
  desired_count        = 1
  subdomain            = "api.agencyportal"
  task_memory          = "2048"
  task_cpu             = "1024"
  enable_lb_stickiness = true
  port_mappings        = [
    {
      protocol      = "tcp"
      containerPort = 8088
    }
  ]
  app_port              = 8088
  health_check_settings = {
    path    = "/actuator/health"
    port    = "8088"
    matcher = "200"
  }
  environment_variables_nonsensitive = {
    AP_FEATURES_REASSIGN            = true
    AP_FEATURES_RFA                 = true
    AP_FEATURES_WITHDRAWAL          = true
    AP_TOKEN_EXPIRY                 = "1200"
    DDS_API_HOST                    = "https://gobiz-document-uat.l1t.molb.gov.sg"
    DDS_APP_ID                      = "G2B_AGENCYPORTAL"
    L1T_API_APP_ID                  = "G2B_AGENCYPORTAL"
    L1T_API_HOST                    = "https://gbl-api-uat.l1t.molb.gov.sg"
    L1T_APP_ID                      = "G2B_L1T"
    L1T_FILE_UPLOAD_SERVICE_HOST    = "http://internal-fu-service-uat-alb-1148589341.ap-southeast-1.elb.amazonaws.com"
    L1T_INTEGRATION_VERSION         = "1.1"
    MOLB_CORS_CORS_WHITELIST        = "https://agencyportal.qa.gobusiness.io"
    MOLB_DB_HOST                    = "ap-backend.cdd3fglgspmk.ap-southeast-1.rds.amazonaws.com"
    MOLB_DB_NAME                    = "agencyportal_backend_qa"
    MOLB_DB_PORT                    = "5432"
    MOLB_DB_ROLE                    = "agencyportal_backend_qa"
    MOLB_HOST                       = "https://api.agencyportal.qa.gobusiness.io"
    MOLB_SPRING_PROFILES_ACTIVE     = "qa"
    MOLB_SQS_URL_L1T_UPDATE_STATUS  = "https://sqs.ap-southeast-1.amazonaws.com/934558626247/qa-ap-l1t-update-status.fifo"
    MOLB_SQS_URL_START_APP_WORKFLOW = "https://sqs.ap-southeast-1.amazonaws.com/934558626247/qa-ap-start-approval-workflow"
    REDIS_PORT                      = "6379"
    REDIS_PRIMARY                   = "master.ap-backend-qa.bm24e0.apse1.cache.amazonaws.com"
    REDIS_REPLICA                   = "replica.ap-backend-qa.bm24e0.apse1.cache.amazonaws.com"
    WOGAD_AUTH_HOST                 = "https://login.microsoftonline.com"
    WOGAD_CLIENT_ID                 = "39505c5d-c5fd-411a-8449-9aa5f3e044d1"
    WOGAD_GRANT_TYPE                = "authorization_code"
    WOGAD_REDIRECT_URI              = "https://agencyportal.gobusiness.io/ssologin"
    WOGAD_SCOPE                     = "openid"
    WOGAD_TENANT_ID                 = "0b11c524-9a1c-4e1b-84cb-6336aefc2243"
  }

  environment_variables_sensitive = {
    AP_TOKEN_SECRET                      = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgG+HAszIyuMop+NlBcIFt08AAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMUBoCXRrY2g68ZgDMAgEQgDsmyGcfb6DePxD6anpA6KczDu6P4IJv7cVNyc7PzWFyHcGsHzVqjQcgl/txiROVApZ4Zr96gNbvo5vM/A=="
    DDS_AUTH_PRIVATE_KEY                 = "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"
    L1T_API_AUTH_PRIVATE_KEY             = "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"
    L1T_AUTH_PUBLIC_KEY                  = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgHoPwXzz5rPDj685BOI4YIkAAAE8DCCBOwGCSqGSIb3DQEHBqCCBN0wggTZAgEAMIIE0gYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAy2UiPii206sR3ozRICARCAggSjxVD3/DNgXNF08RN5M9Jlyq/Ej4FxN5cMBZ8zdpoL0kFUSFFdAxXNDDoLiyzn8WryrtI9Ir82r88svMLH4f1XSOhfvPs1aVuMYlOilw0TG13pchQmUI7skG1/U5mpUTwJieDxdTc5DdcZhPKHmwmil8bumPTfWXwLtZs8/llcmElz949QqurksyIGCRaPTxVd0bjtsMiM42Axkuxsp3517XZ85h6/a0b2UxLQc4RkaG3bCVQeqmjMXZ3TMASnsiggFHpdVwLVAROa+xYTlZBOx0jEp4NwsvN5Wbd/kp4J31HSWN6GQhrq8s2WaEN6urZDed87uEGDtSrXAqnIF24KG82KxKG6gKFlWLrGgJ6Nogi4jl6DfuIiIgbP9qAs0nGViyh3tG2g18RS4/bKKYHp+JrxmwYKZSene5lZNyZz+ipYVYMdKrTyGdVNNUNnAwVCkCndzTCzgODfPSLTwSsg/4zFsP6fFwPHK+S1if6NCISMhX4+KfMdlhFKW4SNXU55mel0EeDFzIcaRiB2d14I+2eJqdD4N97xIWo+o6lZYmOriDQixIccPA8qxh3+dVUiposhCmxmzJ7zajnFB0QSocX+PV3J3BrIQ9EPGQx88K675NYlpOrKZDHcMYJCSGKXA7OWqcbSZ0vRFYksis0LTtluQxrk1EpgUTVnbA3zzgsl0JDR7k65+RrYAU4L+C0tU3YgxuRXWR4DgKZgFkbDqzU1NUR/CsO8F4Ug70yvN+HJVoPEzW4JGyfRABQSUzxsHj0jQEmO5Afwuq2GDZYBx7bHkoNtlNLJitSfHLpNGc+A06F+aMu3xj0TF1siRNOGNcsn9KRfP0cekgqvS9v4IODoeGSIk7+TCQPwhlnFxUtRg/lpQu825aoWQ/pkbkvXvZ2IEmX3nf6oDy+O0yTuN89KtTNNMhMMNXZ7NJi9ZUyKpVqhDfXLNCOl0BWtLwUNPQgMUfHzsVBNzGfVkmIttOLEYC4RjOuSbZLcNRGXfnXouPAC1bnjMKBhhz3vZTZHih50G+cx4ik9MoWQbZmrGWPvjoT0yq8lTtaDSFBgZvtZ7CriHIRp6b2uKQoMxscLL8jq7EtqLJJWmZrkDxdCg0vJfuI7d1bdWMem5zDb5JkruMbskmu50RZmrorWizRUk3Ju+19Mg7g8Yp54niEwLpJStKUxcqcXZihNgRTD3e+BgkXqLnH51/AyPf06qzl+g2+c0GmgOUyrP8MukRvirmumVCp63V32b6g/r58hz+Ah06xf9wbe593X8pmak4Wx4UfkbaWw1Vbt5ceZS8Srbv6havluRz73WIDAdOjsNx0epXosJtyMtj/5w/4xM5MVZC2hRXx3h5Be3kKCRpj+f5TEXw9L3dPS+yxJ/lE48q49A8oR+REImNoIyk/Im09Q3GBFmcLUNgHV3oUpSPMInYmSRMCe2ShgU/YrFM9PxpKsLV5KvUMyBXeAaCnToIRmrwD8fsuuoXYZb+AIYcK82NxG1bmhWhEWezIJX/QfMZhyhmxNj0cFpT+rDSXwmifN3qdO/MmZdcEDbXXDGdmi3TUSpkYTEUCdrZ78JA4AXlRG/2k="
    MOLB_MASKING_PRIVATE_KEY_HEXADECIMAL = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgHUlA8KGmU/U+FAYvVCleDrAAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMwtTmORGJYZQ3TA+gAgEQgDt4cDD5mD9K7iSb6rs8Dr4lqErLYNGfb3coUN7f4wmmYMuHY4b779bgAGr797VcpdhS74fy42Cd6PHHDQ=="
    MOLB_SPRING_SECURITY_JOB_PASSWORD    = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgHw/Scn0sqToou4YbKVbmNXAAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMF9oCIx5jUANwYmn2AgEQgDuzXWdPcgus17ZnMGPwFhEE65pIpmO0GBHLOSnmRldmdMXCWrYWC/Kk5xISQR+nWeA/0MGghJV9V1QqZw=="
    MOLB_SPRING_SECURITY_JOB_USERNAME    = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgG0XCZ8mgt8mH12coEIwsU0AAAAdDByBgkqhkiG9w0BBwagZTBjAgEAMF4GCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQM389QfpIXkdVbJcQRAgEQgDEkIsF94mdHjll9vUC3KigiA55WhnMHSWfm/1xe5PEtpfoXbBYYdZUTbogmbykdRqPf"
    WOGAD_CLIENT_SECRET                  = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgEpowPbDbgyweuEziHaOwGtAAAAhzCBhAYJKoZIhvcNAQcGoHcwdQIBADBwBgkqhkiG9w0BBwEwHgYJYIZIAWUDBAEuMBEEDMN57Qt9Mtl2y6YBXQIBEIBDD7QwDgrswo3oowiXV+gAchNKTZqEXn36XfSpMKcERDZqrxKzwlMynKrhYiXSEtw3FzcDgyEqjErf4BIB97VXTNMAJg=="
    REDIS_TOKEN                          = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgHJ+ktJxsJFDjw5BpBqkLO9AAAAcjBwBgkqhkiG9w0BBwagYzBhAgEAMFwGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMWUW4syVhrAHQqoQzAgEQgC9SVUgoMUbzdivox1rCKw8owr2aJ3h52n4UCPDbYafg9Gek0AhbK+hA4jWaOrq8fw=="
    GSIB_IP                              = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgFF69MbZB3iClTgTLeY4M3qAAAAeDB2BgkqhkiG9w0BBwagaTBnAgEAMGIGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQM+iO3c2mpWdJ04tlXAgEQgDWloA1utQjc7H9hEwaXFW4sL1u/E2VLjwMX/Tc5bacvilXzSpVddOgOE43hlCICpiapxaJfrQ=="
  }

  tags = merge(
    local.common_inputs.tags,
    {
      Cost_Center = "ap-backend"
    }
  )
}
