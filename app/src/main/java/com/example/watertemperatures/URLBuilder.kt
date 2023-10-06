package com.example.watertemperatures

class URLBuilder {
    fun buildUrl(baseUrl: String, path: String, queryParams: Map<String, String>): String {
        val urlBuilder = StringBuilder(baseUrl)

        // Append the path to the URL
        if (!path.startsWith("/")) {
            urlBuilder.append("/")
        }
        urlBuilder.append(path)

        // Append query parameters to the URL
        if (queryParams.isNotEmpty()) {
            urlBuilder.append("?")
            queryParams.forEach { (key, value) ->
                urlBuilder.append(key).append("=").append(value).append("&")
            }
            urlBuilder.deleteCharAt(urlBuilder.length - 1)
        }

        return urlBuilder.toString()
    }
}