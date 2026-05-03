variable "region" {
  type        = string
  description = "AWS region"
  default     = "us-east-1"
}

variable "bucket_name" {
  type        = string
  description = "S3 bucket name for Smart Vault objects"
}

variable "kms_key_alias" {
  type        = string
  description = "Alias name (without 'alias/') for the KMS key"
  default     = "smartvault"
}
