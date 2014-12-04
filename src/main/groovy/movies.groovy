#!/usr/bin/env groovy
def start = System.currentTimeMillis()

// where is the data located
def moviesFileURL = 'http://introcs.cs.princeton.edu/java/data/movies-mpaa.txt'
def moviesFileLocation = '../../../data/movies-mpaa.txt' 
def moviesFile = new File(moviesFileLocation)

if (!moviesFile.exists()) { moviesFile << new URL(moviesFileURL).text } // download file !exists

def movies = moviesFile.readLines().collect {[ 
						'year':(((it.split('\\(')[1]) =~ /([0-9]{4,4}+)/)[0][0]),
						'title':("${it.split('/')[0]}".split('\\(')[0]).trim().split(',').reverse().join(' ').trim(),
						'actors': it.split('/')[1..-1].collect { it.split(',').reverse().join(" ").trim() }
					]} 

// re-usable list of unique years
def years = movies.collect { it['year'] }.unique().sort()

println "Number of movie titles found: " + movies.count { it['title'] }
println "Number of unique movie titles found: " + movies.collect { it['title'] }.unique().size()
println "Movies with actor 'Clint Eastwood': " + movies.findAll { it['actors'].contains('Clint Eastwood') }.collect { it['title'] }.join(', ')
println "Year(s) without movies: " + ((((years.min() as int)..(years.max() as int)).collect { it as String }) - years).join(', ')
println "Number of movies by year: " + (years.collect { year -> ["${year}":(movies.count { it['year'] == year })] })

print "The most active actor of all time is "
actors = [:]
movies.each { movie -> movie['actors'].each { actor -> (!actors[actor]) ? actors[actor] = 1 : actors[actor]++ } }
def mostActiveActor = actors.sort { it.value }.collect { it }.pop()
println mostActiveActor.key + " with " + mostActiveActor.value + " movies."

print "Most active actor by year: "
years.each { year ->
	actors = [:] // reset actors
	movies.findAll { it['year'] == year }.each { movie -> movie['actors'].each { actor -> (!actors[actor]) ? actors[actor] = 1 : actors[actor]++ } }
	def mostActiveActorByYear = actors.sort { it.value }.collect { it }.pop()
	def actorsWithSameActivity = (actors.findAll { it.value == mostActiveActorByYear.value }.collect{ it.key }).unique().sort()
	println year + "\t" + mostActiveActorByYear.value + "\t" + ((actorsWithSameActivity) ? actorsWithSameActivity.join(', ') : '')
	//def actorsWithSameActivity = ((actors.findAll { it.value == mostActiveActorByYear.value }.collect{ it.key }) - mostActiveActorByYear.key).unique().sort()
	//println year + "\t" + mostActiveActorByYear.key + "\t" + mostActiveActorByYear.value + "\t" + ((actorsWithSameActivity) ? actorsWithSameActivity.join(', ') : '')
}

println "Time to exec: " + ((System.currentTimeMillis() - start)/1000)