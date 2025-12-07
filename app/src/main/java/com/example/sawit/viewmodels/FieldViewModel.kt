package com.example.sawit.viewmodels

import androidx.lifecycle.ViewModel
import com.example.sawit.models.Field
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class   FieldViewModel : ViewModel() {
    //    private val fieldsData = mutableListOf<Field>()
    private val _fieldsData = MutableStateFlow<List<Field>>(emptyList())
    val fieldsData: StateFlow<List<Field>> = _fieldsData.asStateFlow()

    init {
        populateData()
    }

    private fun populateData() {
        val fieldsList = mutableListOf<Field>()

        fieldsList.add(
            Field(
                fieldId = 1,
                fieldPhoto = "ada",
                fieldName = "Lahan 1",
                fieldArea = 1270.0,
                fieldLocation = "Ngabang",
                avgOilPalmAgeInMonths = 12,
                oilPalmType = "Tenera",
                fieldDesc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent ut hendrerit felis. Aliquam ut odio enim. Vestibulum convallis convallis bibendum. Ut id purus vel libero porttitor volutpat. Pellentesque placerat finibus arcu quis congue. Aenean et eros ac dolor pharetra placerat quis pharetra quam. Donec fermentum consectetur aliquet. Vestibulum commodo velit ac dui gravida, efficitur malesuada ligula scelerisque. Pellentesque rhoncus scelerisque risus quis efficitur. Phasellus placerat lorem lectus, non malesuada nunc facilisis a. Quisque ullamcorper enim sit amet lacus convallis rutrum. Quisque venenatis sapien ac dolor dapibus, eget pretium purus eleifend. Fusce pellentesque velit in tellus finibus consectetur eget blandit purus.",
                notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque dolor mi, fermentum fringilla tempus ac, pretium quis turpis. Donec interdum eget purus quis blandit. Class.",
            )
        )

        fieldsList.add(
            Field(
                fieldId = 2,
                fieldPhoto = null,
                fieldName = "Lahan Manjur Sukses",
                fieldArea = 550.10,
                fieldLocation = "Sosok",
                avgOilPalmAgeInMonths = 37,
                oilPalmType = "Topaz",
                fieldDesc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc ullamcorper elit mauris, eu lacinia nunc scelerisque a. Nam a tincidunt lacus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.",
                notes = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque dolor mi, fermentum fringilla tempus ac, pretium quis turpis. Donec interdum eget purus quis blandit. Class.",
            )
        )
//
//        fieldsList.add(
//            Field(
//                fieldId = 3,
//                fieldPhoto = null,
//                fieldName = "",
//                fieldArea = 0.0,
//                fieldLocation = "",
//                avgOilPalmAgeInMonths = 0,
//                oilPalmType = "0",
//                fieldDesc = "0",
//                notes = "",
//            )
//        )
        _fieldsData.value = fieldsList.take(3)
    }
}