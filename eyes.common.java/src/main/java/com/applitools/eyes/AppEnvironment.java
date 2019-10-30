package com.applitools.eyes;

/**
 * The environment in which the application under test is executing.
 */
public class AppEnvironment {
    private String inferred;
    private String os;
    private String hostingApp;
    private RectangleSize displaySize;
    private String deviceInfo;

    /**
     * Creates a new AppEnvironment instance.
     */
    public AppEnvironment() {
    }

    /**
     * Creates a new AppEnvironment instance.
     *
     * @param inferred the inferred environment information.
     */
    public AppEnvironment(String inferred) {
        this.inferred = inferred;
    }


    /**
     * Creates a new AppEnvironment instance.
     *
     * @param displaySize the display size of the application or {@code null} if unknown.
     * @param os          the OS hosting the application under test or {@code null} if
     *                    unknown.
     * @param hostingApp  the application hosting the application under test or {@code null}
     *                    * if unknown.
     */
    public AppEnvironment(String os, String hostingApp,
                          RectangleSize displaySize) {
        setOs(os);
        setHostingApp(hostingApp);
        setDisplaySize(displaySize);
    }

    /**
     * @return the information inferred from the execution environment or {@code null} if no
     * information could be inferred.
     */
    public String getInferred() {
        return inferred;
    }

    /**
     * @param inferred -  the inferred environment information.
     */
    public void setInferred(String inferred) {
        this.inferred = inferred;
    }

    /**
     * @return the OS hosting the application under test or {@code null} if
     * unknown.
     */
    public String getOs() {
        return os;
    }

    /**
     * @param os -  the OS hosting the application under test or {@code null} if
     *           unknown.
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * @return the application hosting the application under test or {@code null}
     * if unknown.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getHostingApp() {
        return hostingApp;
    }

    /**
     * @param hostingApp -  the application hosting the application under test or {@code null}
     *                   if unknown.
     */
    public void setHostingApp(String hostingApp) {
        this.hostingApp = hostingApp;
    }

    /**
     * @return the display size of the application or {@code null} if unknown.
     */
    public RectangleSize getDisplaySize() {
        return displaySize;
    }

    /**
     * @param size -  the display size of the application or {@code null} if unknown.
     */
    public void setDisplaySize(RectangleSize size) {
        this.displaySize = size;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    @Override
    public String toString() {
        return "[os = " + (os == null ? "?" : "'" + os + "'") + " hostingApp = "
                + (hostingApp == null ? "?" : "'" + hostingApp + "'")
                + " deviceInfo = " + (deviceInfo == null ? "?" : "'" + deviceInfo + "'")
                + " displaySize = " + displaySize + "]";
    }
}
