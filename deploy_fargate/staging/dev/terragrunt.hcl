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
  environment_name     = "dev"
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
    DDS_API_HOST                    = "https://gobiz-document-dev.l1t.molb.gov.sg"
    DDS_APP_ID                      = "G2B_AGENCYPORTAL"
    L1T_API_APP_ID                  = "G2B_AGENCYPORTAL"
    L1T_API_HOST                    = "https://gbl-api-dev.l1t.molb.gov.sg"
    L1T_APP_ID                      = "G2B_L1T"
    L1T_FILE_UPLOAD_SERVICE_HOST    = "http://internal-fu-service-dev-alb-186550976.ap-southeast-1.elb.amazonaws.com"
    L1T_INTEGRATION_VERSION         = "1.1"
    MOLB_CORS_CORS_WHITELIST        = "https://agencyportal.dev.gobusiness.io"
    MOLB_DB_HOST                    = "ap-backend.cdd3fglgspmk.ap-southeast-1.rds.amazonaws.com"
    MOLB_DB_NAME                    = "agencyportal_backend_dev"
    MOLB_DB_PORT                    = "5432"
    MOLB_DB_ROLE                    = "agencyportal_backend_dev"
    MOLB_HOST                       = "https://api.agencyportal.dev.gobusiness.io"
    MOLB_SPRING_PROFILES_ACTIVE     = "dev"
    MOLB_SQS_URL_L1T_UPDATE_STATUS  = "https://sqs.ap-southeast-1.amazonaws.com/934558626247/dev-ap-l1t-update-status.fifo"
    MOLB_SQS_URL_START_APP_WORKFLOW = "https://sqs.ap-southeast-1.amazonaws.com/934558626247/dev-ap-start-approval-workflow"
    REDIS_PORT                      = "6379"
    REDIS_PRIMARY                   = "master.ap-backend-dev.bm24e0.apse1.cache.amazonaws.com"
    REDIS_REPLICA                   = "replica.ap-backend-dev.bm24e0.apse1.cache.amazonaws.com"
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
    MOLB_SPRING_SECURITY_JOB_PASSWORD    = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgEvWnVJntx76hvVL6iRgyE0AAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMpiR/l7esmhiKJUr4AgEQgDsedjIRGP4VmDpjEZ2hZQxZXlgZr/K+2kEoDGVj9sugKGsUeL6Pgi1pbMG+ilZVkHrqxWJwzVaQaq8K0w=="
    MOLB_SPRING_SECURITY_JOB_USERNAME    = "AQICAHhX5Bu6JxI4nOyGjWm4KardO68/V4xoaB69qB4ug8FZlgECwBJjt0eutwP9IOEHZgBuAAAAdTBzBgkqhkiG9w0BBwagZjBkAgEAMF8GCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMUkejFsJbxDpBLS80AgEQgDKOvfG/PSlypKNoKs1wloZC5Itncc9tz5NTearAH6dy7npfdeasQ1VvBs+jEtAhAB1Oyw=="
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
