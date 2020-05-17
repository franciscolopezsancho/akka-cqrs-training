# AKKA CQRS 
The gist of this exercises is to get familiar with event sourcing through [akka persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html)

This assumes some familiarity with Akka Typed (at least what a Behavior is). And also a vague idea of what problem event sourcing and akka persistence try to solve.


from the previous we assume tables have been created

1. please test that's the case that you can run the test and prove that 
   remember you'll 

2. Let's add now a projection. First just print it.
   latet alppakka to stream it
   a bit of theory: A projection is like a shadow. It's a transformation from a Source.
   And when saying source here we use the streams semantics. AS Source -> flow -> Sink. So a projection is a stream. When querying the journal we will read the data as a stream with backpressure. Here's a bit of info:  https://doc.akka.io/docs/akka/current/stream/stream-flows-and-basics.html


3. Let's now insert that in a DB. Let's add the projection in the init-table.sql
   we'll test that manually for now.
      - for this we'll use a slick session and the result from the query in Step 2 as the Source to iterate over.
         https://doc.akka.io/docs/alpakka/current/slick.html#using-a-slick-flow-or-sink
      - and before run the test let's get into the docker mysql and run the creation ot the table `my-projection`

4. let's automate the test now and refactor. 
      Will need three things. 

      A. As tests on BoxSpec we'll need to create a persistence Entity and send an Item to it
      B. As previous test in Projection Spec you'll need to read the journal and insert in db
      
      nothing new till here
      C. Read from the my_projection table as a stream to verify we have what we expected. I recommend to print it first
      https://doc.akka.io/docs/alpakka/current/slick.html#using-a-slick-source

      D. Once we see how values come from the projection use `eventually { ... result should contain ("yada yada") ...}
      You'll need mixint the test class `with Eventually` before begin able to add 
      
      Hints: be wary of the boxId you won't be able to insert it twice

      but after all we'll have to create all tables in case of deploying the app. So ...
      Let's first rerun mysql script to clean to clean everything and 

5. Let's refactor
   Take the creation from the projection out and put its own file
   Create 

4. also for cassandra?

5. also for Kafka?. 

6. Let's write by tag
      you may want to have a look at 
      Tags are there for you to be able to work with multiple entities. When you query the journal you may filter this
      way the entities you're processor is interested in. 

6.1. Now let's make the tag be useful not only to filter one type of entities. Now multiple processors should be able to consume the same persistence entity but each processor will process only specifics id. We'll need to define a function to define the 'specifics' such as := def partitionTag(persistenceId: String): String = {"box-tag" concat (hash(id) mod num-consumers)}. i.e partitionTag("123") => "box-tag-0" . 

    I would recommend first add this "num-consumers" as a hardcoded, 2 would be just fine.  Then test all this with two processors consuming each one one event. 

    Then after test passes. Let's refactor and make the num-consumers and also "box-tag" string come from application.conf.

    

6.2 Now let's tag by event, and filter by that. 


6. Let's consume in parallel.

7. Let's manage the offset


