# Python: movie queries.
# December 12th, 2014
# Rob Hooft

class MovieSink(object):
    def __init__(self):
        pass
    def add(self, title, year, appendix, player):
        pass
    def report(self):
        print "No-op"

class MovieCounter(MovieSink):
    def __init__(self):
        self.n = 0
    def add(self, title, year, appendix, player):
        self.n += 1
    def report(self):
        print "Total number of movies %d" % self.n

class DistinctTitles(MovieSink):
    def __init__(self):
        self.s = set()
    def add(self, title, year, appendix, player):
        self.s.add(title)
    def report(self):
        print "Total number of distinct titles %d" % len(self.s)

class ClintFinder(MovieSink):
    def __init__(self):
        self.n = 0
    def add(self, title, year, appendix, player):
            if 'Eastwood, Clint' in player:
                    self.n += 1
    def report(self):
        print "Clint Eastwood in %d" % self.n

class DistinctYears(MovieSink):
    def __init__(self):
        self.s = set()
    def add(self, title, year, appendix, player):
        self.s.add(year)
    def report(self):
        print "Distinct years %d" % len(self.s)

class MostBusyYear(MovieSink):
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
        print "Busiest year %s with %d movies" % (busy, highest)

class ProlificActor(MovieSink):
    def __init__(self):
        self.s = {}
    def add(self, title, year, appendix, player):
        for p in player:
            if self.s.has_key(p):
                self.s[p] += 1
            else:
                self.s[p] = 1
    def report(self):
        highest = 0
        for k,v in self.s.items():
            if v > highest:
                prol = [k]
                highest = v
            elif v == highest:
                prol.append(k)
        print "Most Prolific Actor %s with %d movies" % (prol, highest)

class ProlificActorYear(MovieSink):
    def __init__(self):
        self.s = {}
    def add(self, title, year, appendix, player):
        for p in player:
            if self.s.has_key((p,year)):
                self.s[(p,year)] += 1
            else:
                self.s[(p,year)] = 1
    def report(self):
        highest = 0
        for k,v in self.s.items():
            if v > highest:
                prol = [k]
                highest = v
            elif v == highest:
                prol.append(k)
        print "Most Prolific Actor in one year %s with %d movies" % (prol, highest)

class Fork(MovieSink):
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
            year = year[:4]
            appendix = year[5:]
            player = players.strip().split('/')
            self.sink.add(title, year, appendix, player)

f = Fork()
f.newsink(MovieCounter())
f.newsink(DistinctTitles())
f.newsink(ClintFinder())
f.newsink(DistinctYears())
f.newsink(ProlificActor())
f.newsink(ProlificActorYear())
f.newsink(MostBusyYear())

FileSource(f).read('../../../data/movies-mpaa.txt')
f.report()
