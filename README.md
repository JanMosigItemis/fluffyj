# Fluffy J
A library that tries to put a little fluff ❤ into the daily life of Java developers.
  
During the last couple of years I found myself writing the same kind of helper code over and over again. Especially when it comes to dealing with Java concurrency.  
Of course anything this library does may also be done by the help of [Guava](https://github.com/google/guava) and co. However, these libraries are at times a bit hard to comprehend or do miss some minor thing that I grew fond of. So I decided to come up with a couple of helpers of my own.  
  
This is not a replacement for any of the popular helper libraries out there. Nothing fancy here. Only fluff.  
  
The fluff should help to make your code easier to comprehend and more stream lined.

## Build
`mvn clean install`

# Throwing Exceptions Without The Need To Declare Them
Have a look at `de.itemis.mosig.fluffyj.sneaky.Sneaky`.  
  
In some edge case situations it might be easier to just throw any kind of Throwable instead of wrapping it in some kind of RuntimeException. This is especially true for things like Suppliers that do not allow checked exceptions to be thrown. Wrapping and unwrapping is a real pain. With Sneaky, you may do so:
  
```
// This method throws an IOException even though it does not declare it with {@code throws}.

  public void operation() {
    Sneaky.throwThat(new IOException());
  }
```
  
This kind of thing is usually a smell and only valid in a few situations. Please take care when doing so.

# Exceptions
`ImplementationProblemException` may be used to indicate a mistake in the implementation, e. g. a missing default branch in a switch statement. Developers tend to throw a RuntimeException in such a case. `ImplementationProblemException` aims at providing a consistent default way to handle these kind of things.  
  
`InstantiationNotPermittedException` aims at providing a default way to indicate that a constructor may not be used. This may be used to make sure, a class is never instantiated. This is usually helpful to make sure static helpers are not instantiated by some "smart" reflective code, e. g.:  
  
```
public final class Clazz() {
    private Clazz() {
        throw new InstantionNotPermittedException();
    }
}
```
  
## ThrowablePrettyfier
When handling Throwables (usually Exceptions) they often get logged.
