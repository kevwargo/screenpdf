package org.kevwargo.screenpdf.winextra;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.win32.W32APIOptions;


public interface WinGDIExtra extends WinGDI {

    public DWORD SRCCOPY = new DWORD(0x00CC0020);

}
