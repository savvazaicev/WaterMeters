package com.company.watermeters.model

data class Client(
    var fullName: String?,
    var address: String?,
    var registryNumber: String?,
    var number: String?,
    var endDate: String?,
    var waterType: String?,
    var certificateNumber: String?,
    var imagesURLs: MutableList<String>,
    var email: String?
)