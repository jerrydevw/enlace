#!/bin/bash
awslocal sqs create-queue --queue-name provisioning-queue
awslocal s3 mb s3://enlace-recordings
echo "LocalStack initialized: SQS queue and S3 bucket created."
