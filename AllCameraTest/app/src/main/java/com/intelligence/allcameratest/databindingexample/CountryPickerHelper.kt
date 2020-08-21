package com.intelligence.allcameratest.databindingexample

import android.text.TextUtils
import com.intelligence.allcameratest.recyclerviewutil.BaseDataBinding
import java.util.*
import kotlin.collections.ArrayList

class CountryPickerHelper {
    companion object {
        val entityList = ArrayList<CountryName?>()
        val letterSet = HashSet<LetterEntity>()
        val letterArrays = ArrayList<String>()

        fun sortCountryListWithLetter(entities: List<CountryName?>?): ArrayList<String>?{
            if (entities == null) return null
            entityList.clear()
            entityList.addAll(entities)
            letterSet.clear()
            for (entity in entities) {
                val sortName = entity!!.getSortName()
                if (!TextUtils.isEmpty(sortName)) {
                    var letter = sortName!!.get(0)
                    letterSet.add(LetterEntity(letter + ""))
                }
            }
            entityList.addAll(letterSet)
            Collections.sort(entityList) { o1, o2 ->
                val sortName: String = o1!!.getSortName()!!.toLowerCase()
                val anotherSortName: String = o2!!.getSortName()!!.toLowerCase()
                return@sort sortName.compareTo(anotherSortName)
            }
            letterArrays.clear()
            entityList.forEach {
                if (it?.getEnName()?.length == 1){
                    letterArrays.add(it.getEnName()!!)
                }
            }
            return letterArrays
        }
    }
}