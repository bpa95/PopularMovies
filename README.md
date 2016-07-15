# PopularMovies
Part of Android Developer Nanodegree

### The Movie Database API Key is required
app/build.gradle:20:22:
```gradle
    buildTypes.each {
        it.buildConfigField 'String', 'MOVIE_DATABASE_API_KEY', YourMovieDatabaseApiKey
    }
```
Replace YourMovieDatabaseApiKey by you own API Key from themoviedb.org.
If you don’t already have an account, you will need to [create one](https://www.themoviedb.org/account/signup) in order to request an API Key.
In your request for a key, state that your usage will be for educational/non-commercial use.
You will also need to provide some personal information to complete the request.
Once you submit your request, you should receive your key via email shortly after.
