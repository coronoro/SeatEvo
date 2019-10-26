package freeplan

data class BoardEntry (
    val name: String,
    val type: String,
    val boardId: Int,
    val stopId: Int,
    val stopName: String,
    val dateTime: String,
    val origin: String,
    val track: String,
    val detailsId: String
){
}