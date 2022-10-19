package util

val coroutineName: String
    get() {
        val regex = Regex(".+ @(.+)#.+")
        val matches = regex.find(Thread.currentThread().name)!!
        return matches.groups[1]!!.value
    }