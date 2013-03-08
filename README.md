# Actors Need Parental Supervision

This repo provides code & notes for the March 2013 Iowa Scala Enthusiasts meetup [Actors Need Parental Supervision](http://www.meetup.com/ia-scala/events/104733812/).

## References

- [Akka documentation](http://doc.akka.io/docs/akka/2.0.5/)
 - [Supervision and Monitoring](http://doc.akka.io/docs/akka/2.0.5/general/supervision.html)
 - [Fault Tolerance](http://doc.akka.io/docs/akka/2.0.5/scala/fault-tolerance.html)
- [Akka in Action](http://www.manning.com/roestenburg/), Chapter 3 Fault Tolerance
 - Source code from the book is available at the above page

## Questions to Answer

- How does Akka help you build a fault-tolerant system?
- What does "let it crash" mean?
- What is a supervisor hierarchy and how can you take advantage of it?
- How should you isolate a dangerous operation in an actor, handle things when it blows up and keep moving forward?
- What do you do when a Future completes with an exception instead of a value?

## Lessons Learned (#akkaprotips)

- A Future[T] really is more like a future Either[Throwable, T] than a future T
 - Learn about & always remember how the Throwable persists across map/flatMap/sequence/etc
  - Mapping a failed future results in another failed future
  - Sequencing futures in which any of them fail results in a failed future
 - You wouldn't call either.right.get; to be safe you'd call either.right getOrElse default
 - So to be safe, always do future fallbackTo Future(default), or future recover { case t: Throwable => default }
 - Use recover if you need the exception to compute the value; use fallbackTo to just provide a default value
- A future obtained by asking an actor can timeout; a future obtained directly from Future.apply will not timeout
- An actor that replies to sender (to complete an ask future) must handle all possible messages and always reply, otherwise the future will time out
