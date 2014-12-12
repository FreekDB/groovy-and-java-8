# Python: movie queries.
# December 12th, 2014
# Rob Hooft

class MovieCounter(object):
    doc = "Total number of movies %d"
    def __init__(self):
        self.n = 0
    def add(self, title, year, appendix, player):
        self.n += 1
    def report(self):
        print self.doc % self.n

class DistinctTitles(MovieCounter):
    doc = "Total number of distinct titles %d"
    def __init__(self):
        self.s = set()
    def add(self, title, year, appendix, player):
        self.s.add(title)
    def report(self):
        print self.doc % len(self.s)

class ClintFinder(MovieCounter):
    doc = "Clint Eastwood in %d"
    def add(self, title, year, appendix, player):
        if 'Eastwood, Clint' in player:
            self.n += 1

class DistinctYears(DistinctTitles):
    doc = "Distinct years %d"
    def add(self, title, year, appendix, player):
        self.s.add(year)

class MostBusyYear(MovieCounter):
    doc = "Busiest year %s with %d movies"
    def __init__(self):
        self.s = {}
    def add(self, title, year, appendix, player):
        if self.s.has_key(year):
            self.s[year] += 1
        else:
            self.s[year] = 1
    def report(self):
        highest = 0
        for k, v in self.s.items():
            if v > highest:
                busy = [k]
                highest = v
            elif v == highest:
                busy.append(k)
        print self.doc % (busy, highest)

class YearsWithoutMovies(MostBusyYear):
    doc = "Years without movies %s"
    def report(self):
        yrs = sorted(self.s.keys())
        nomovies = []
        for y in range(yrs[0], yrs[-1]+1):
            if not self.s.has_key(y):
                nomovies.append(y)
        print self.doc % nomovies

class ProlificActor(MostBusyYear):
    doc = "Most prolific actor %s with %d movies"
    def add(self, title, year, appendix, player):
        for p in player:
            if self.s.has_key(p):
                self.s[p] += 1
            else:
                self.s[p] = 1

class ProlificActorYear(MostBusyYear):
    doc = "Most prolific actor in one year %s with %d movies"
    def add(self, title, year, appendix, player):
        for p in player:
            if self.s.has_key((p,year)):
                self.s[(p,year)] += 1
            else:
                self.s[(p,year)] = 1

class Fork(MovieCounter):
    def __init__(self):
        self.inferior = []
    def newsink(self, sink):
        self.inferior.append(sink)
    def add(self, title, year, appendix, player):
        for c in self.inferior:
            c.add(title, year, appendix, player)
    def report(self):
        for c in self.inferior:
            c.report()

class FileSource(object):
    def __init__(self, sink):
        self.sink = sink
    def read(self, fn):
        for line in open(fn):
            title, players = line.split('/', 1)
            title, year = title.split('(', 1)
            assert year[-1]==')', year
            appendix = year[5:]
            year = int(year[:4])
            player = players.strip().split('/')
            self.sink.add(title, year, appendix, player)

f = Fork()
f.newsink(MovieCounter())
f.newsink(DistinctTitles())
f.newsink(ClintFinder())
#f.newsink(DistinctYears())
f.newsink(YearsWithoutMovies())
f.newsink(ProlificActor())
f.newsink(ProlificActorYear())
f.newsink(MostBusyYear())

FileSource(f).read('../../../data/movies-mpaa.txt')
f.report()
