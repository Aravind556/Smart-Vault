output "cluster_name" {
  value       = aws_eks_cluster.main.name
  description = "EKS Cluster Name"
}

output "cluster_endpoint" {
  value       = aws_eks_cluster.main.endpoint
  description = "EKS Cluster Endpoint"
}

output "cluster_version" {
  value       = aws_eks_cluster.main.version
  description = "EKS Cluster Version"
}

output "cluster_security_group_id" {
  value       = aws_security_group.cluster.id
  description = "EKS Cluster Security Group ID"
}

output "oidc_provider_arn" {
  value       = aws_iam_openid_connect_provider.cluster.arn
  description = "OIDC Provider ARN for IRSA"
}

output "kubectl_config_command" {
  value       = "aws eks update-kubeconfig --name ${aws_eks_cluster.main.name} --region ${var.region}"
  description = "Command to configure kubectl"
}
