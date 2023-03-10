include {
  path = find_in_parent_folders()
}

locals {
  git_path     = trimspace(run_cmd("git", "rev-parse", "--show-toplevel"))
  is_use_local = tobool(get_env("LOCAL", false))
  common_inputs = read_terragrunt_config(find_in_parent_folders("common.hcl")).inputs

  local_source  = "${local.git_path}/../molb-gcc-infra//modules/ecs_service"
  remote_source = "git::git@github.com:gdsace/molb-gcc-infra.git//modules/ecs_service?ref=${get_env("DEPLOY_REF", "master")}"
}

terraform {
  source = local.is_use_local ? local.local_source : local.remote_source
}

inputs = {
  external             = true
  service_name         = "ap-backend"
  environment_name     = "prod"
  desired_count        = 2
  subdomain            = "api.agencyportal"
  task_memory          = "2048"
  task_cpu             = "1024"
  enable_lb_stickiness = true
  port_mappings = [{
    protocol      = "tcp"
    containerPort = 8088
  }]
  app_port = 8088
  health_check_settings = {
    path    = "/actuator/health"
    port    = "8088"
    matcher = "200"
  }
  environment_variables_nonsensitive = {
    AP_FEATURES_REASSIGN            = false
    AP_FEATURES_RFA                 = false
    AP_FEATURES_WITHDRAWAL          = false
    AP_TOKEN_EXPIRY                 = "1200"
    DDS_API_HOST                    = "https://document.gobusiness.gov.sg"
    DDS_APP_ID                      = "G2B_AGENCYPORTAL"
    L1T_API_APP_ID                  = "G2B_AGENCYPORTAL"
    L1T_API_HOST                    = "https://api.gbl.gobusiness.gov.sg"
    L1T_APP_ID                      = "G2B_L1T"
    L1T_FILE_UPLOAD_SERVICE_HOST    = "http://internal-fu-service-prod-alb-1248238092.ap-southeast-1.elb.amazonaws.com"
    L1T_INTEGRATION_VERSION         = "1.1"
    MOLB_CORS_CORS_WHITELIST        = "https://agencyportal.gobusiness.gov.sg"
    MOLB_DB_HOST                    = "ap-backend.cpevxccz2i9n.ap-southeast-1.rds.amazonaws.com"
    MOLB_DB_NAME                    = "agencyportal_backend_production"
    MOLB_DB_PORT                    = "5432"
    MOLB_DB_ROLE                    = "agencyportal_backend_production"
    MOLB_HOST                       = "https://api.agencyportal.gobusiness.gov.sg"
    MOLB_SPRING_PROFILES_ACTIVE     = "production"
    MOLB_SQS_URL_L1T_UPDATE_STATUS  = "https://sqs.ap-southeast-1.amazonaws.com/150825224711/prod-ap-l1t-update-status.fifo"
    MOLB_SQS_URL_START_APP_WORKFLOW = "https://sqs.ap-southeast-1.amazonaws.com/150825224711/prod-ap-start-approval-workflow"
    REDIS_PORT                      = "6379"
    REDIS_PRIMARY                   = "master.ap-backend-production.qivzao.apse1.cache.amazonaws.com"
    REDIS_REPLICA                   = "replica.ap-backend-production.qivzao.apse1.cache.amazonaws.com"
    WOGAD_AUTH_HOST                 = "https://login.microsoftonline.com"
    WOGAD_CLIENT_ID                 = "125d5ca6-0682-405e-9421-708eeb667909"
    WOGAD_GRANT_TYPE                = "authorization_code"
    WOGAD_REDIRECT_URI              = "https://agencyportal.gobusiness.gov.sg/ssologin"
    WOGAD_SCOPE                     = "openid"
    WOGAD_TENANT_ID                 = "0b11c524-9a1c-4e1b-84cb-6336aefc2243"
  }

  environment_variables_sensitive = {
    AP_TOKEN_SECRET                      = "AQICAHiWe1jNPnRu61pWNz3mAkC6eYe866Hq4JyizDwg+fZ0EwGOUp3xeEFBrrSuj8rcPk1CAAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQM6CLD1scTTAe70yrqAgEQgDuLs20qaHOoJNCxEHOqjYl6wk6rfuPhV3g7mIfEilofPTqo8ttHdJxM+TIrCJPX6aWVcjTKGYwfDQT8fQ=="
    DDS_AUTH_PRIVATE_KEY                 = "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"
    L1T_API_AUTH_PRIVATE_KEY             = "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"
    L1T_AUTH_PUBLIC_KEY                  = "AQICAHiWe1jNPnRu61pWNz3mAkC6eYe866Hq4JyizDwg+fZ0EwHfGM8uvU8pRt7r2WR/LHrAAAAEmDCCBJQGCSqGSIb3DQEHBqCCBIUwggSBAgEAMIIEegYJKoZIhvcNAQcBMB4GCWCGSAFlAwQBLjARBAwtwfZis4pDG6H8dLMCARCAggRL+gFuRB5w89rBHinSRHJdttK05xE7ycQexZlXFVBR88qnoErK5LWsWfD66CHQ8Jnnvwhh1cjUkycqcghrZGtfnKQCeorwod+FYoTn6/kELZ0yuodk4YB5nPbImd+gJtd4QuEHr4qe016Ovk7Xc57ClYeoV2SC7too+BmOUy7faiTjTuaBi4HLigGv50o6UWr1CPB6PmBF/fqLNIVpzbJsw9vGsCgJ1P7FfRj1EJycSMau7DiGC4pcAZjiKmE3tOvKhAmClCvcQje6Li0+ydheRSrM42ng7kcDar5JATeNn60TSohiCg521xTL0MoPB2gAx0on/8rR8jRYOE6UKm5QFCSf6/vk68pWAd6tKZO1PWvfkKXa59rJ1DSMKPB+KbK4HuLb0Mod8gJZtJswPYEqYubSfA0LfjX11wtXgU7z4MzoBAidLneTFSpP2ChXk2MnVXsTDx5XbeStYzrn0vMNOcltsl8Y5DLLrPaV/8NeoGIWfpG8wdp15uzWefELsOu0Wi0BOx3yIIH/CbJwoxLGNELMGK0I/wJ7YASzOCdrdqD4HBOiGjwTgP3Jy1sB1o7eSJd07O5CYFlGrCJwz9Y/6qZi2mRJZ+WCRn69vAo8g/EMFt+nia+oFQYBMz5FMZlmNUZ6A0gKrOzImGe4E+HKtlexWrlMfk3+9HHIxN9tb3KXFmEFp+cFEN9zoM9M5q8Q4eeFRW7lYFQsXG00mFi7PWsGm/gMOh7fgaFwlEQ0xMAmq3XDhxILAp/tDwK8usQLrHnBFojikvV7eO21qEFHKxYNblN8oar07ZW5/X9L1y3pOEBTQBhRVHjHjYsJs/Di1E/x+vnxLZMaGJCk8Uyj/jbO/OXuYzaxJpF195jRFV8lXLryL8xU4EWfJKmQLfItExQMGCitdf8YEPqXh2tEzyipkI8KSd0jXYz/DxPK+EzKwHA8wXM/3L4uyREFQ7GRk3TOzwsuNJCdtKfUGt2hzXdZMFJIFA0A1GEcwHB54Gy4dtFf5llVgUABbZi5EYoVNiO8rAeeSQvIE+WkZV+QiNI5zzuODQXGT4gEsBTRju9KGxzgB8OO53IXGyfkW8Spk+kDSF45kqSBc6K98CH+NCnsNx8IbmUPCZ2jh1/QNxdnWem6IxwQbFH2xQTunmaDZv8GtVdQPw3MlP7dfbcsPcXIpr7soxC2izqGOhwItEE4Ukk+MHntRFQAYKcA1zCliINy0iGNSxrS3HURXrsZ8hMT4LAwBlBOAQktlfHCtUXVAXMsuQ2mtJAqNr++s7R2YIIPg7M+gbzrsGa21EuUoZD1GPRAkpn9MLd/XiIuodO2O+1AwtX2RQigl+jqtv2y5PHs6+gCUEbCA/qaA9kyqDNLEy20oLxrIlOfUc5HTh26JdpxY+Jt7f+a7L3KLhnac6/OQARYkrCMAsv2WaU5yUh2bZnEVuOKjK/mBrSbsiCtFcI/xNCJDEYtFA=="
    MOLB_MASKING_PRIVATE_KEY_HEXADECIMAL = "AQICAHiWe1jNPnRu61pWNz3mAkC6eYe866Hq4JyizDwg+fZ0EwF7vnwgt1fqhICDss+bpPAHAAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMaaxJEbgSPzopF2n/AgEQgDt9MK2NzD0sa/hTXsOGreY20Bz7paaNJ9eWDARBuspEkyCY/IsMwwHuDq507mUf5PtauP3eysg7wbZSag=="
    MOLB_SPRING_SECURITY_JOB_PASSWORD    = "AQICAHiWe1jNPnRu61pWNz3mAkC6eYe866Hq4JyizDwg+fZ0EwERRYClIaJoUOBQj3P1a+KZAAAAfjB8BgkqhkiG9w0BBwagbzBtAgEAMGgGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQM/cd1eHk/13/nj41rAgEQgDtiAcO6x7UXHw3mFGguQvH1RLIUsBjBNKcA4s6ftma4K1BHLolYLD8VCILyoOUvqjzi/O8k8D+uEqRvEA=="
    MOLB_SPRING_SECURITY_JOB_USERNAME    = "AQICAHiWe1jNPnRu61pWNz3mAkC6eYe866Hq4JyizDwg+fZ0EwGRKx6rCDPQru/egr9KzlwGAAAAfDB6BgkqhkiG9w0BBwagbTBrAgEAMGYGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMH+25ywYbqmJUUQyGAgEQgDnGJ6K/LybSJj9oliyImSF0pnUMuYvKefw9+0+9eKYifrQo34Rc6T3ZYx5aK98eyr9Jm5EN1cYgBh8="
    WOGAD_CLIENT_SECRET                  = "AQICAHiWe1jNPnRu61pWNz3mAkC6eYe866Hq4JyizDwg+fZ0EwH07vtCXEb5i7XRu7FHqSmKAAAAhzCBhAYJKoZIhvcNAQcGoHcwdQIBADBwBgkqhkiG9w0BBwEwHgYJYIZIAWUDBAEuMBEEDO+9MSSOzdcX1zqoSgIBEIBDkve5YCMnQ7I8cws5CQfQoXWhnl1brK9QZTeCxFNSFaVDgSt1lt06sesS+sJ0PGBzjGzZ6WuTov7cXjBeJhRBSHmQAQ=="
    REDIS_TOKEN                          = "AQICAHiWe1jNPnRu61pWNz3mAkC6eYe866Hq4JyizDwg+fZ0EwHQc8b4TMzC1JyRtShgHoF1AAAAcjBwBgkqhkiG9w0BBwagYzBhAgEAMFwGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMqVykQQYI7/c5b/6qAgEQgC9ggAfq+lc3ApM5FkLpfgUyKiVTtP0ojseGPxLn5LbBHPaoE3wK5Hf7Ydz1Yu9ZfQ=="
    GSIB_IP                              = "AQICAHiWe1jNPnRu61pWNz3mAkC6eYe866Hq4JyizDwg+fZ0EwGY+sfgNZw78MIO7Um+M5snAAAAeDB2BgkqhkiG9w0BBwagaTBnAgEAMGIGCSqGSIb3DQEHATAeBglghkgBZQMEAS4wEQQMoXTJJ8C7zmjTOyA2AgEQgDUVTtnW5fxl3Kudau4nnfziaqlZ0J12Nq4mxN44PA7wKgNGt660Jqcu3khqaj4cCIb+5Jb3lw=="
  }
  tags = merge(
    local.common_inputs.tags,
    {
      Cost_Center = "ap-backend"
    }
  )
}
