package dafi.pipeline

private case class A()
private case class B(a: A)
private case class C(a: A, b: B)

class PipelineTest extends munit.FunSuite:

  test("simplePipeline"):
    val builder = PipelineBuilder()
      .addFactory[A](pipeline => () => A())
      .addFactory[B](pipeline =>
        val stepA = pipeline.step[A]
        () => B(stepA.get())
      ).addFactory[C](pipeline =>
        val stepA = pipeline.step[A]
        val stepB = pipeline.step[B]
        () => C(stepA.get(), stepB.get()))
    val pipeline = builder.newPipeline
    val c = pipeline.step[C].get()
    val b = pipeline.step[B].get()
    val a = pipeline.step[A].get()
    assert(c.a eq a)
    assert(c.b eq b)
    assert(b.a eq a)
