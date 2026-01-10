package com.example.xliteapp

data class Tweet(
    val id: String? = null,
    val uid: String? = null,
    val username: String? = null,
    val content: String? = null,
    val timestamp: Long? = 0,
    var likeCount: Int = 0,
    var replyCount: Int = 0,
    var retweetCount: Int = 0
)