package dafi.ctfs

import dafi.steps.NormalStop

case class CtfsStop(pid: Int, eid: Int, gid: Int, normalStop: NormalStop)
