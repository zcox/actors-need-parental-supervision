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
