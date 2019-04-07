package me.gladwell.cisco.aws

case class AwsInstance(
  name: String,
  instanceType: String,
  state: String,
  az: String,
  ip: String,
  privateIp: String)
