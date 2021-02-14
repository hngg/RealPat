package com.gegepad.modtrunk.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;

public class NetUtils
{
		private static String TAG = NetUtils.class.getSimpleName();

//		public static String getIPAddress() {
//			return getIPAddress(GApplication.mContext);
//		}
		
	    public static String getIPAddress(Context context) {
	    	if(context==null)
	    		return null;
	        NetworkInfo info = ((ConnectivityManager) context
	                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	        if (info != null && info.isConnected()) {
	            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
	                try {
	                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
	                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
	                        NetworkInterface intf = en.nextElement();
	                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
	                            InetAddress inetAddress = enumIpAddr.nextElement();
	                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
	                                return inetAddress.getHostAddress();
	                            }
	                        }
	                    }
	                } catch (SocketException e) {
	                    e.printStackTrace();
	                }

	            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
	                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
	                return ipAddress;
	            }
	        } else {
	            //当前无网络连接,请在设置中打开网络
	        }
	        return "192.168.43.1";
	    }

	    /**
	     * 将得到的int类型的IP转换为String类型
	     *
	     * @param ip
	     * @return
	     */
	    public static String intIP2StringIP(int ip) {
	        return (ip & 0xFF) + "." +
	                ((ip >> 8) & 0xFF) + "." +
	                ((ip >> 16) & 0xFF) + "." +
	                (ip >> 24 & 0xFF);
	    }

	@SuppressLint("NewApi")
	public static boolean CodecSupportTyte(String type)//"video/avc"->h264 "video/hevc"->h265
	{
		boolean bSupport = false;
		Log.e(TAG, "mediacodec build version:" + Build.VERSION.SDK_INT + " codecCount:" + MediaCodecList.getCodecCount());
		if(Build.VERSION.SDK_INT>=18)
		{
			for(int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--)
			{
				MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);

				String[] types = codecInfo.getSupportedTypes();

				for (int i = 0; i < types.length; i++)
				{
					Log.e(TAG, "mediacodec SupportedTypes:" + types[i]);
					if (types[i].equalsIgnoreCase(type))
					{
						bSupport = true;
					}
				}
			}
		}
		return bSupport;
	}

	public static void YUV420spRotate90(byte[] src, byte[] dst, int srcWidth, int srcHeight)
	{
		int wh = srcWidth * srcHeight;
		int uvHeight = srcHeight >> 1;//uvHeight = height / 2

		//旋转Y
		int k = 0, start = (srcHeight - 1)*srcWidth;
		for(int i = 0; i < srcWidth; i++) {
			int nPos = start;
			for(int j = 0; j < srcHeight; j++) {
				dst[k] = src[nPos + i];
				k++;
				nPos -= srcWidth;
			}
		}

		wh += (uvHeight-1)*srcWidth;
		for(int i = 0; i < srcWidth; i+=2){
			int nPos = wh;
			for(int j = 0; j < uvHeight; j++) {
				dst[k] = src[nPos + i];
				dst[k + 1] = src[nPos + i + 1];
				k += 2;
				nPos -= srcWidth;
			}
		}
	}

	public static void NV21ToNV12(byte[] nv21,byte[] nv12,int width,int height)
	{
		if(nv21 == null || nv12 == null)return;
		int framesize = width*height;
		int i = 0,j = 0;
		System.arraycopy(nv21, 0, nv12, 0, framesize);
		for(i = 0; i < framesize; i++)
			nv12[i] = nv21[i];
		for (j = 0; j < framesize/2; j+=2)
			nv12[framesize + j-1] = nv21[j+framesize];
		for (j = 0; j < framesize/2; j+=2)
			nv12[framesize + j] = nv21[j+framesize-1];
	}

	public static void nv21ToI420(byte[] yv21bytes, byte[] i420bytes, int width, int height) {
		int total = width * height;
		ByteBuffer bufferY = ByteBuffer.wrap(i420bytes, 0, total);
		ByteBuffer bufferU = ByteBuffer.wrap(i420bytes, total, total / 4);
		ByteBuffer bufferV = ByteBuffer.wrap(i420bytes, total + total / 4, total / 4);

		bufferY.put(yv21bytes, 0, total);
		for (int i=total; i<yv21bytes.length; i+=2) {
			bufferV.put(yv21bytes[i]);
			bufferU.put(yv21bytes[i+1]);
		}
	}

	public void swapU_V(byte[] src, int width, int height) {
		byte tmp;
		int index = width * height;
		for (int i = 0; i < width * height / 2; ) {
			tmp = src[index + i];
			src[index + i] = src[index + i + 1];
			src[index + i + 1] = tmp;
			i += 2;
		}
	}


	private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width,int height) {
		System.arraycopy(yv12bytes, 0, i420bytes, 0, width * height);
		System.arraycopy(yv12bytes, width * height + width * height / 4, i420bytes, width * height, width * height / 4);
		System.arraycopy(yv12bytes, width * height, i420bytes, width * height + width * height / 4, width * height / 4);
	}

	public static byte[] swapNV12toNV21(final byte[] input, final int offset, final byte[] output, final int width, final int height) {
		final int frameSize = width * height;
		final int qFrameSize = frameSize / 2;

		System.arraycopy(input, offset, output, 0, frameSize); // Y

		for (int i = 0; i + 1 < qFrameSize; i += 2) {
			output[frameSize + i] = input[offset + frameSize + i + 1]; // U
			output[frameSize + i + 1] = input[offset + frameSize + i]; // V
		}
		return output;
	}

	public byte[] NV21toYUV420Planar(final byte[] input, final int offset, final byte[] output, final int width, final int height) {
		final int frameSize = width * height;
		final int qFrameSize = frameSize / 4;

		System.arraycopy(input, offset, output, 0, frameSize); // Y

		for (int i = 0; i < qFrameSize; i++) {
			output[frameSize + i] = input[offset + frameSize + i * 2 + 1]; // U
			output[frameSize + qFrameSize + i] = input[offset + frameSize + i * 2]; // V
		}

		return output;
	}

	public static byte[] NV12toYUV420Planar(final byte[] input, final int offset, final byte[] output, final int width, final int height) {
		final int frameSize = width * height;
		final int qFrameSize = frameSize / 4;

		System.arraycopy(input, offset, output, 0, frameSize); // Y

		for (int i = 0; i < qFrameSize; i++) {
			output[frameSize + i] = input[offset + frameSize + i * 2]; // U
			output[frameSize + qFrameSize + i] = input[offset + frameSize + i * 2 + 1]; // V
		}
		return output;
	}

	public static byte[] YV12toYUV420SemiPlanar(final byte[] input, final int offset, final byte[] output, final int width, final int height) {
		/*
		 * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12 We convert by putting
		 * the corresponding U and V bytes together (interleaved).
		 */
		final int frameSize = width * height;
		final int qFrameSize = frameSize / 4;

		System.arraycopy(input, offset, output, 0, frameSize); // Y

		for (int i = 0; i < qFrameSize; i++) {
			output[frameSize + i * 2] = input[offset + frameSize + i + qFrameSize]; // Cb (U)
			output[frameSize + i * 2 + 1] = input[offset + frameSize + i]; // Cr (V)
		}
		return output;
	}

	public static byte[] I420toYUV420SemiPlanar(final byte[] input, final int offset, final byte[] output, final int width, final int height) {
		/*
		 * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12 We convert by putting
		 * the corresponding U and V bytes together (interleaved).
		 */
		final int frameSize = width * height;
		final int qFrameSize = frameSize / 4;

		System.arraycopy(input, offset, output, 0, frameSize); // Y

		for (int i = 0; i < qFrameSize; i++) {
			output[frameSize + i * 2] = input[offset + frameSize + i]; // Cb (U)
			output[frameSize + i * 2 + 1] = input[offset + frameSize + i + qFrameSize]; // Cr (V)
		}
		return output;
	}

	public static byte[] I420toNV21(final byte[] input, final int offset, final byte[] output, final int width, final int height) {
		/*
		 * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12 We convert by putting
		 * the corresponding U and V bytes together (interleaved).
		 */
		final int frameSize = width * height;
		final int qFrameSize = frameSize / 4;

		System.arraycopy(input, offset, output, 0, frameSize); // Y

		for (int i = 0; i < qFrameSize; i++) {
			output[frameSize + i * 2 + 1] = input[offset + frameSize + i]; // Cb (U)
			output[frameSize + i * 2 + 1] = input[offset + frameSize + i + qFrameSize]; // Cr (V)
		}
		return output;
	}

	public static void CropYUV420SemiPlanar(final byte[] input, final int width, final int height, final byte[] output,
											final int crop_left, final int crop_right, final int crop_top, final int crop_bottom) {

		for(int i = crop_top; i <= crop_bottom; i++) {
			System.arraycopy(input, i * width + crop_left, output, i * (crop_right - crop_left + 1), crop_right - crop_left + 1); // Y
		}

		for(int i = crop_top; i <= crop_bottom / 2; i++) {
			System.arraycopy(input, width * height + i * width + crop_left, output,
					(crop_right - crop_left + 1) * (crop_bottom - crop_top + 1) + i * (crop_right - crop_left + 1),
					crop_right - crop_left + 1); // Y
		}
	}

	public static void CropYUV420Planar(final byte[] input, final int width, final int height, final byte[] output,
										final int crop_left, final int crop_right, final int crop_top, final int crop_bottom) {

		for(int i = crop_top; i <= crop_bottom; i++) {
			System.arraycopy(input, i * width + crop_left, output, i * (crop_right - crop_left + 1), crop_right - crop_left + 1); // Y
		}

		for(int i = crop_top; i <= crop_bottom / 2; i++) {
			System.arraycopy(input, width * height + i * width / 2 + crop_left,
					output,((crop_right - crop_left + 1) * (crop_bottom - crop_top + 1) + i * (crop_right - crop_left + 1) / 2),
					(crop_right - crop_left + 1) / 2); // U
		}

		for(int i = crop_top; i <= crop_bottom / 2; i++) {
			System.arraycopy(input, width * height / 4 * 5 + i * width / 2 + crop_left,
					output,((crop_right - crop_left + 1) * (crop_bottom - crop_top + 1) / 4 * 5 + i * (crop_right - crop_left + 1) / 2),
					(crop_right - crop_left + 1) / 2); // V
		}
	}

}