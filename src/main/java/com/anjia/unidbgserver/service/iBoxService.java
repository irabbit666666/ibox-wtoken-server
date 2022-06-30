package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import com.anjia.unidbgserver.utils.TempFileUtils;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.EmulatorBuilder;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.DynarmicFactory;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.api.ApplicationInfo;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.file.ByteArrayFileIO;
import com.github.unidbg.linux.file.RandomFileIO;
import com.github.unidbg.memory.Memory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.anjia.unidbgserver.utils.Base64Utils;



// Language: java
@Slf4j
public class iBoxService extends AbstractJni {

    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    private final DvmClass TigerTallyAPI;

    private final static String BASE_IBOX_PATH = "data/apks/ibox/";
    private final static String IBOX_APK_PATH = BASE_IBOX_PATH + "ibox.apk";

    private final UnidbgProperties unidbgProperties;

    private final static String LIBTT_ENCRYPT_LIB_PATH = "data/apks/ibox/lib/libtiger_tally.so";

    @SneakyThrows iBoxService(UnidbgProperties unidbgProperties) {
        this.unidbgProperties = unidbgProperties;
        // 创建模拟器实例，要模拟32位或者64位，在这里区分
        EmulatorBuilder<AndroidEmulator> builder = AndroidEmulatorBuilder.for64Bit().setProcessName("com.box.art");
        // 动态引擎
        if (unidbgProperties.isDynarmic()) {
            builder.addBackendFactory(new DynarmicFactory(true));
        }
        emulator = builder.build();
        // 模拟器的内存操作接口
        final Memory memory = emulator.getMemory();
        // 设置系统类库解析
        memory.setLibraryResolver(new AndroidResolver(23));

        // 创建Android虚拟机
        // vm = emulator.createDalvikVM(); // 只创建vm，用来读so,不加载apk
        vm = emulator.createDalvikVM(TempFileUtils.getTempFile(IBOX_APK_PATH));
        // 设置是否打印Jni调用细节
        vm.setVerbose(unidbgProperties.isVerbose());
        vm.setJni(this);
        // 加载libttEncrypt.so到unicorn虚拟内存，加载成功以后会默认调用init_array等函数，这是直接读so文件
         DalvikModule dm = vm.loadLibrary(TempFileUtils.getTempFile(LIBTT_ENCRYPT_LIB_PATH), false);
        // 这是搜索加载apk里的模块名，比如 libguard.so 那么模块名一般是guard
//        DalvikModule dm = vm.loadLibrary("tiger_tally", false);
        // 手动执行JNI_OnLoad函数
        dm.callJNI_OnLoad(emulator);
        // 加载好的libttEncrypt.so对应为一个模块
        module = dm.getModule();

        dm.callJNI_OnLoad(emulator);
        this.TigerTallyAPI = vm.resolveClass("com/aliyun/TigerTally/TigerTallyAPI");
        emulator.getSyscallHandler().setEnableThreadDispatcher(true);
        try {
            int r = this.TigerTallyAPI.callStaticJniMethodInt(this.emulator, "_genericNt1(ILjava/lang/String;)I", 1, new StringObject(vm, "EWA40T3eMNVkLmj8Ur9CuQExbcOti8c3yd-I8xDkLhvphNMuRujkY7V6lKbvAtE2qXa4kTWSnXmo0HXfuUXRgyFNXYwhwvvf7yUYQ-DjWjAa34fjA9yJCam4Llddmcu3D8BQKw4gR-nkYzzOx0uGj9OkfgUHoFxF00akZNyeMrs="));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        char c = '\uffff';
        switch (signature.hashCode()) {
            case -1128258774:
                if (signature.equals("android/content/pm/PackageInfo->versionName:Ljava/lang/String;")) {
                    c = 1;
                    break;
                }
                break;
            case 636580376:
                if (signature.equals("android/content/pm/PackageInfo->signatures:[Landroid/content/pm/Signature;")) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                DvmObject sig = vm.resolveClass("android/content/pm/Signature", new DvmClass[0]).newObject(null);
                return new ArrayObject(sig);
            case 1:
                return new StringObject(vm, "1.1.4");
            default:
                return super.getObjectField(vm, dvmObject, signature);
        }
    }

    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        char c = '\uffff';
        switch (signature.hashCode()) {
            case -1197621879:
                if (signature.equals("android/os/Build->PRODUCT:Ljava/lang/String;")) {
                    c = 3;
                    break;
                }
                break;
            case -740929009:
                if (signature.equals("android/os/Build->MODEL:Ljava/lang/String;")) {
                    c = 1;
                    break;
                }
                break;
            case -675408929:
                if (signature.equals("android/os/Build->MANUFACTURER:Ljava/lang/String;")) {
                    c = 2;
                    break;
                }
                break;
            case -381171340:
                if (signature.equals("android/os/Build->FINGERPRINT:Ljava/lang/String;")) {
                    c = 0;
                    break;
                }
                break;
            case -219314630:
                if (signature.equals("android/os/Build->DEVICE:Ljava/lang/String;")) {
                    c = 5;
                    break;
                }
                break;
            case 884030085:
                if (signature.equals("android/os/Build$VERSION->RELEASE:Ljava/lang/String;")) {
                    c = 6;
                    break;
                }
                break;
            case 2132457713:
                if (signature.equals("android/os/Build->BRAND:Ljava/lang/String;")) {
                    c = 4;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return new StringObject(vm, "Xiaomi/sagit/sagit:8.0.0/OPR1.170623.027/2017.12.20_aosp-debug:eng/test-keys");
            case 1:
                return new StringObject(vm, "MI 6");
            case 2:
            case 3:
            case 4:
            case 5:
                return new StringObject(vm, "Xiaomi");
            case 6:
                return new StringObject(vm, "10");
            default:
                return super.getStaticObjectField(vm, dvmClass, signature);
        }
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        char c = '\uffff';
        switch (signature.hashCode()) {
            case -1908542658:
                if (signature.equals("android/content/pm/PackageManager->getApplicationLabel(Landroid/content/pm/ApplicationInfo;)Ljava/lang/CharSequence;")) {
                    c = 4;
                    break;
                }
                break;
            case -1598072093:
                if (signature.equals("android/content/pm/PackageManager->getApplicationInfo(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;")) {
                    c = 3;
                    break;
                }
                break;
            case -1269481676:
                if (signature.equals("java/io/File->getAbsolutePath()Ljava/lang/String;")) {
                    c = 7;
                    break;
                }
                break;
            case -564609007:
                if (signature.equals("android/app/Application->getFilesDir()Ljava/io/File;")) {
                    c = 6;
                    break;
                }
                break;
            case -161937328:
                if (signature.equals("android/content/Context->getPackageName()Ljava/lang/String;")) {
                    c = 1;
                    break;
                }
                break;
            case 649065402:
                if (signature.equals("android/content/Context->getFilesDir()Ljava/io/File;")) {
                    c = 5;
                    break;
                }
                break;
            case 1673153102:
                if (signature.equals("java/util/Locale->getLanguage()Ljava/lang/String;")) {
                    c = 0;
                    break;
                }
                break;
            case 1782456369:
                if (signature.equals("android/content/pm/Signature->toByteArray()[B")) {
                    c = 2;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return new StringObject(vm, "zh");
            case 1:
                return new StringObject(vm, "com.box.art");
            case 2:
                try {
                    return new ByteArray(vm, Base64Utils.decrypt("MIIDezCCAmOgAwIBAgIEC1BJXDANBgkqhkiG9w0BAQsFADBuMQswCQYDVQQGEwI4NjEQMA4GA1UECBMHQmVpSmluZzEQMA4GA1UEBxMHQmVpSmluZzEYMBYGA1UEChMPTkZUQk9YIFBURS5MVEQuMRIwEAYDVQQLDAlBbHBoYSBSJkQxDTALBgNVBAMTBGlCb3gwHhcNMjEwNjIxMDY0MjUxWhcNNDYwNjE1MDY0MjUxWjBuMQswCQYDVQQGEwI4NjEQMA4GA1UECBMHQmVpSmluZzEQMA4GA1UEBxMHQmVpSmluZzEYMBYGA1UEChMPTkZUQk9YIFBURS5MVEQuMRIwEAYDVQQLDAlBbHBoYSBSJkQxDTALBgNVBAMTBGlCb3gwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCa2lyfCNRWl8M9dri2+mLfMxy7ea8arZrP3To8pTEZEZtiTDmXDTgBsVLuZzyecb5sBzfC4aQVw5x5+vOFp52vsdIeW7wxfJbgxpcxhCWF8SZgKLoCFc4obZEoEbbSa2dGorukN7JFOkpY7H35VriVv/a+T4alxCEhSXoTd7SPIhtbNA5a5sbKthmjiON4xyapva0+QqAknuGaCikt6c8K1V7hQvF2uo0VVtd6WRi5jFmeP5siQg/8nuKEBbUfe3I7b3pFb9/9aO5SEPCPbkbwL2DGS9vsKl06plLJGgRPtSSJ7tQ6QIqazWFO7hUr/sehin/SYYUe9k2Q+V2hkwQzAgMBAAGjITAfMB0GA1UdDgQWBBRT/cYqO7oPhqnGQItnEgaYkW4LTjANBgkqhkiG9w0BAQsFAAOCAQEAAgP07wz8xkCjioqFmoafuAWQdN8O85EODLc37DIW7uWRZkyuJZX3gx063hFrJSrxkuYMVaX0c8ibusvXw3eApnHcwqW7Ag4K8rrD6rEVxIKDAmHOkD8q9D5IzS+IsFLzoEiKcgMJY2PiK6mfzP8myoB6of9vGrKM3ciIxU9IvibYxwEjghRUhOoOxGM6Lie2Ovij7YT1D6RUFn70mSmvTqTrxi3CGIlOtvlmKj7FmLnl5EodJpQoD+3HoEaviRPSFrBUzcsO6gWWjQ6Y6/0HKWF3OpIhNWHynsETP5a15hY1RZE19/X/qnl0LspHHVh1Gwr5qG9iRlxHB9WeZ9D1GQ=="));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            case 3:
                return new ApplicationInfo(vm);
            case 4:
                return new StringObject(vm, "iBox");
            case 5:
            case 6:
                return vm.resolveClass("java/io/File").newObject(null);
            case 7:
                return new StringObject(vm, "/data/data/com.box.art/files");
            default:
                return super.callObjectMethodV(vm, dvmObject, signature, vaList);
        }
    }

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        char c = '\uffff';
        switch (signature.hashCode()) {
            case -997489950:
                if (signature.equals("java/lang/ClassLoader->loadClass(Ljava/lang/String;)Ljava/lang/Class;")) {
                    c = 3;
                    break;
                }
                break;
            case 563630060:
                if (signature.equals("java/util/Locale->getDefault()Ljava/util/Locale;")) {
                    c = 2;
                    break;
                }
                break;
            case 808607145:
                if (signature.equals("com/aliyun/TigerTally/A->pb(Ljava/lang/String;[B)Ljava/lang/String;")) {
                    c = 1;
                    break;
                }
                break;
            case 1805058527:
                if (signature.equals("com/aliyun/TigerTally/A->ct()Landroid/content/Context;")) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return vm.resolveClass("android/content/Context").newObject(null);
            case 1:
                return new StringObject(vm, "wS8O4RYy64fZSJqmsPYWVT3K5+hweouz0YPvsxAs7x1mfWj0mqidyOwOBffV+mDcI9L0i2JLGp3YHbJYhxir0A==");
            case 2:
                return vm.resolveClass("java/util/Locale" ).newObject(signature);
            case 3:
                StringObject name = (StringObject) vaList.getObjectArg(0);
                System.out.println("findClass className: " + name.getValue());
                return vm.resolveClass("java/lang/Class" ).newObject(name);
            default:
                return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
        }
    }

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        char c = '\uffff';
        switch (signature.hashCode()) {
            case 563630060:
                if (signature.equals("java/util/Locale->getDefault()Ljava/util/Locale;")) {
                    c = 0;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return vm.resolveClass("java/util/Locale").newObject(signature);
            default:
                return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
        }
    }



    public String getWToken(String str) {
        System.out.println("input: " + str);
        List<Object> list = new ArrayList<>(2);
        list.add(1);
        list.add(new ByteArray(vm, str.getBytes(StandardCharsets.UTF_8)));
        StringObject ret = (StringObject) this.TigerTallyAPI.callStaticJniMethodObject(this.emulator, "_genericNt3(I[B)Ljava/lang/String;", list.toArray());
        String result = ret.getValue();
        result=result.replace(result.substring(158, 174), "");
        System.out.println("Wtoken: " + result);
        return result;
    }

    public Object doWork(Object param) {
        return getWToken((String) param);
    }


    public void destroy() throws IOException {
        emulator.close();
    }

    public static void main(String[] args) {
        iBoxService service = new iBoxService(new UnidbgProperties());
        service.getWToken("{\"phoneNumber\":\"13333333333\"}");
    }
}
