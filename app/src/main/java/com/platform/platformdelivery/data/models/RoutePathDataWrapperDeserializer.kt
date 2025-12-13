package com.platform.platformdelivery.data.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class RoutePathDataWrapperDeserializer : JsonDeserializer<RoutePathDataWrapper> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): RoutePathDataWrapper {
        if (json == null || !json.isJsonObject) {
            return RoutePathDataWrapper()
        }

        val jsonObject = json.asJsonObject
        
        // Handle the "data" field - it can be either an object or an empty array
        val dataElement = jsonObject.get("data")
        val nestedData: RoutePathNestedData? = when {
            dataElement == null -> null
            dataElement.isJsonArray -> {
                // If it's an array (empty or not), return null for nestedData
                // This handles the case: {"data": []}
                null
            }
            dataElement.isJsonObject -> {
                // If it's an object, try to deserialize it as RoutePathNestedData
                try {
                    context?.deserialize(dataElement, RoutePathNestedData::class.java)
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
        
        // Handle headdata
        val headdataElement = jsonObject.get("headdata")
        val headdata: HeadData? = if (headdataElement != null && headdataElement.isJsonObject) {
            try {
                context?.deserialize(headdataElement, HeadData::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        
        // Handle status
        val statusElement = jsonObject.get("status")
        val status: Boolean? = if (statusElement != null && statusElement.isJsonPrimitive) {
            try {
                statusElement.asBoolean
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
        
        return RoutePathDataWrapper(
            nestedData = nestedData,
            headdata = headdata,
            status = status
        )
    }
}

