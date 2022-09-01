package com.adan.gpsdemo;

 import android.os.Parcel;
 import android.os.Parcelable;

/**
 * 卫星数组类，2022.3.1  使用Parcelable插件自动生成
 */

/**
 * @hide
 */
public class SvStatusArrays implements Parcelable {
    int b[];    //卫星号
    int b1[];   //卫星类型
    int b2[];   //可用卫星,信噪比>0为1，否则为0
    float c[];  //信噪比
    float d[];  //卫星高程
    float e[];  //方位角
    float f[];  //载波频率

    public SvStatusArrays(){}

    public SvStatusArrays(int b[], int b1[], int b2[],
                          float c[], float d[], float e[], float f[]) {
        this.b=b;
        this.b1=b1;
        this.b2=b2;
        this.c=c;
        this.d=d;
        this.e=e;
        this.f=f;
    }
    /**
     * 获取卫星数量
     * @return
     */
    public int getSvCount(){
        if(b==null) return 0;
        return b.length;
    }
    public int[] getB() {
        return b;
    }

    public void setB(int[] b) {
        this.b = b;
    }

    public int[] getB1() {
        return b1;
    }

    public void setB1(int[] b1) {
        this.b1 = b1;
    }

    public int[] getB2() {
        return b2;
    }

    public void setB2(int[] b2) {
        this.b2 = b2;
    }

    public float[] getC() {
        return c;
    }

    public void setC(float[] c) {
        this.c = c;
    }

    public float[] getD() {
        return d;
    }

    public void setD(float[] d) {
        this.d = d;
    }

    public float[] getE() {
        return e;
    }

    public void setE(float[] e) {
        this.e = e;
    }

    public float[] getF() {
        return f;
    }

    public void setF(float[] f) {
        this.f = f;
    }

    public static Creator<SvStatusArrays> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(this.b);
        dest.writeIntArray(this.b1);
        dest.writeIntArray(this.b2);
        dest.writeFloatArray(this.c);
        dest.writeFloatArray(this.d);
        dest.writeFloatArray(this.e);
        dest.writeFloatArray(this.f);
    }

    public void readFromParcel(Parcel source) {
        this.b = source.createIntArray();
        this.b1 = source.createIntArray();
        this.b2 = source.createIntArray();
        this.c = source.createFloatArray();
        this.d = source.createFloatArray();
        this.e = source.createFloatArray();
        this.f = source.createFloatArray();
    }



    protected SvStatusArrays(Parcel in) {
        this.b = in.createIntArray();
        this.b1 = in.createIntArray();
        this.b2 = in.createIntArray();
        this.c = in.createFloatArray();
        this.d = in.createFloatArray();
        this.e = in.createFloatArray();
        this.f = in.createFloatArray();
    }

    public static final Creator<SvStatusArrays> CREATOR = new Creator<SvStatusArrays>() {
        @Override
        public SvStatusArrays createFromParcel(Parcel source) {
            return new SvStatusArrays(source);
        }

        @Override
        public SvStatusArrays[] newArray(int size) {
            return new SvStatusArrays[size];
        }
    };
}
