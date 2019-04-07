package me.gladwell.cisco.aws

import com.amazonaws.auth.{ AWSCredentials, AWSCredentialsProvider }
import com.amazonaws.regions.RegionUtils

import scala.concurrent.{ ExecutionContext, Future }
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder
import com.amazonaws.services.ec2.model.{ DescribeInstancesRequest, Reservation }

import scala.collection.JavaConverters._

object AwsInstances extends Instances {

  private case class MyAwsCredentialsProvider(key: AwsKey) extends AWSCredentialsProvider {

    override def getCredentials: AWSCredentials = new AWSCredentials {
      override def getAWSAccessKeyId: String = key.id
      override def getAWSSecretKey: String = key.key
    }

    def refresh(): Unit = {}

  }

  def forRegion(region: String)(implicit ec: ExecutionContext, key: AwsKey) = Future {
    val ec2 = AmazonEC2ClientBuilder
      .standard()
      .withCredentials(new MyAwsCredentialsProvider(key))
      .withRegion(region)
      .build()

    val request = new DescribeInstancesRequest
    val response = ec2.describeInstances(request)

    for {
      reservation <- response.getReservations().asScala
      instance <- reservation.getInstances().asScala
    } yield AwsInstance(
      name = instance.getTags.asScala.find(_.getKey == "Name").map(_.getKey).getOrElse(""),
      instanceType = instance.getInstanceType,
      state = instance.getState.getName,
      az = instance.getPlacement.getAvailabilityZone,
      ip = instance.getPublicIpAddress,
      privateIp = instance.getPrivateIpAddress)
  }

}
