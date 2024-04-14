package com.example.androidsmartdevice

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult

/**
 * This class is a custom implementation of the ScanCallback class.
 * It provides custom actions for when a scan result is received, when a batch of scan results is received, and when a scan fails.
 *e
 * @property onScanResultAction The action to perform when a scan result is received.
 * @property onBatchScanResultAction The action to perform when a batch of scan results is received.
 * @property onScanFailedAction The action to perform when a scan fails.
 */
class BleScanCallback(
    private val onScanResultAction: (ScanResult?) -> Unit = {},
    private val onBatchScanResultAction: (MutableList<ScanResult>?) -> Unit = {},
    private val onScanFailedAction: (Int) -> Unit = {}
) : ScanCallback() {

    /**
     * This function is called when a BLE advertisement has been found.
     *
     * @param callbackType The type of callback that triggered this function.
     * @param result The scan result that contains information about the remote device as well as the scan record of the advertisement.
     */
    override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
        super.onScanResult(callbackType, result)
        onScanResultAction(result)
    }

    /**
     * This function is called to deliver batch scan results.
     * These are BLE advertisements that were previously scanned.
     *
     * @param results List of scan results that are previously scanned.
     */
    override fun onBatchScanResults(results: MutableList<android.bluetooth.le.ScanResult>?) {
        super.onBatchScanResults(results)
        onBatchScanResultAction(results)
    }

    /**
     * This function is called when scan could not be started.
     *
     * @param errorCode The error code related to the failure.
     */
    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        onScanFailedAction(errorCode)
    }
}