provider "aws" {
  region = var.region
}

resource "aws_kms_key" "smartvault" {
  description             = "Smart Vault master key for envelope encryption"
  deletion_window_in_days = 7
  enable_key_rotation     = true
}

resource "aws_kms_alias" "smartvault" {
  name          = "alias/${var.kms_key_alias}"
  target_key_id = aws_kms_key.smartvault.key_id
}

resource "aws_s3_bucket" "smartvault" {
  bucket = var.bucket_name
}

resource "aws_s3_bucket_versioning" "smartvault" {
  bucket = aws_s3_bucket.smartvault.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "smartvault" {
  bucket = aws_s3_bucket.smartvault.id
  rule {
    apply_server_side_encryption_by_default {
      kms_master_key_id = aws_kms_key.smartvault.arn
      sse_algorithm     = "aws:kms"
    }
  }
}
