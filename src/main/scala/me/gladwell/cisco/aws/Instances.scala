package me.gladwell.cisco.aws

import scala.concurrent.{ ExecutionContext, Future }

trait Instances {

  def forRegion(region: String)(implicit ec: ExecutionContext, key: AwsKey): Future[Seq[AwsInstance]]

}
