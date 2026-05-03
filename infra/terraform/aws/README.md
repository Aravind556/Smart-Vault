# Terraform (AWS)

This folder provisions:

- An S3 bucket (versioning enabled)
- A KMS key + alias
- S3 default SSE-KMS using that key

Example:

```bash
cd infra/terraform/aws
terraform init
terraform apply -var="region=ap-south-1" -var="bucket_name=my-smartvault-bucket"
```

Use outputs:

- `kms_key_arn` for `SMARTVAULT_KMS_KEY_ID`
- `bucket_name` for `SMARTVAULT_S3_BUCKET`

Then configure the backend:

```powershell
$env:SMARTVAULT_KEY_PROVIDER="aws-kms"
$env:SMARTVAULT_S3_REGION="ap-south-1"
$env:SMARTVAULT_S3_BUCKET="<bucket_name output>"
$env:SMARTVAULT_KMS_KEY_ID="<kms_key_arn output>"
```
