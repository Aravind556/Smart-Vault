output "bucket_name" {
  value = aws_s3_bucket.smartvault.bucket
}

output "kms_key_arn" {
  value = aws_kms_key.smartvault.arn
}
