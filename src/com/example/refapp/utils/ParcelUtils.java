package com.example.refapp.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ParcelUtils {
	public static boolean readBoolean(Parcel source) {
		return source.readInt()==1;
	}

	public static void writeBoolean(Parcel dest, boolean value) {
		dest.writeInt(toInt(value));
	}

	public static <E extends Enum<E>> E readEnum(Parcel source, Class<E> enumType) {
		String name = source.readString();
		if (name == null) return null;
		return Enum.valueOf(enumType, name);
	}

	public static <E extends Enum<E>> void writeEnum(Parcel dest, E value) {
		dest.writeString(value==null? null : value.name());
	}

	public static <T extends Parcelable> ArrayList<T> readTypeArrayList(
			Parcel source,
			Parcelable.Creator<T> creator) {
		boolean isNull = readBoolean(source);
		if (isNull) {
			return null;
		} else {
			return source.createTypedArrayList(creator);
		}
	}

	public static <T extends Parcelable> void writeTypeList(Parcel dest, List<T> list) {
		boolean isNull = list == null;
		writeBoolean(dest, isNull);
		if (!isNull) {
			dest.writeTypedList(list);
		}		
	}
	
	public static Date readDate(Parcel source) {
		return (Date)source.readValue(Date.class.getClassLoader());
	}

	public static void writeDate(Parcel dest, Date value) {
		dest.writeValue(value);
	}

	public static BigDecimal readBigDecimal(Parcel source) {
		return readValue(source, BigDecimal.class);
	}

	public static void writeBigDecimal(Parcel dest, BigDecimal value) {
		writeValue(dest, value);
	}

	public static <T> T readValue(Parcel source, Class<T> resultType) {
		return (T)source.readValue(resultType.getClassLoader());
	}

	public static <T> void writeValue(Parcel dest, T value) {
		dest.writeValue(value);
	}

	public static int toInt(boolean value) {
		return value? 1 : 0;
	}
}
