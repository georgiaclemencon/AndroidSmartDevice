package com.example.androidsmartdevice

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.widget.Toast

/**
 * Cette classe est une implémentation personnalisée de la classe ScanCallback.
 * Elle fournit des actions personnalisées pour quand un résultat de scan est reçu, quand un lot de résultats de scan est reçu, et quand un scan échoue.
 *
 * @property onScanResultAction L'action à effectuer lorsqu'un résultat de scan est reçu.
 * @property onBatchScanResultAction L'action à effectuer lorsqu'un lot de résultats de scan est reçu.
 * @property onScanFailedAction L'action à effectuer lorsqu'un scan échoue.
 */
class BleScanCallback(
    private val onScanResultAction: (ScanResult?) -> Unit = {},
    private val onBatchScanResultAction: (MutableList<ScanResult>?) -> Unit = {},
    private val onScanFailedAction: (Int) -> Unit = {}
) : ScanCallback() {

    /**
     * Cette fonction est appelée lorsqu'une publicité BLE a été trouvée.
     *
     * @param callbackType Le type de rappel qui a déclenché cette fonction.
     * @param result Le résultat du scan qui contient des informations sur le périphérique distant ainsi que l'enregistrement du scan de la publicité.
     */
    override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult?) {
        super.onScanResult(callbackType, result)
        onScanResultAction(result)
    }

    /**
     * Cette fonction est appelée pour livrer des résultats de scan en lot.
     * Ce sont des publicités BLE qui ont été précédemment scannées.
     *
     * @param results Liste des résultats de scan qui ont été précédemment scannés.
     */
    override fun onBatchScanResults(results: MutableList<android.bluetooth.le.ScanResult>?) {
        super.onBatchScanResults(results)
        onBatchScanResultAction(results)
    }

    /**
     * Cette fonction est appelée lorsque le scan n'a pas pu être démarré.
     *
     * @param errorCode Le code d'erreur lié à l'échec.
     */
    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
//        Toast.makeText(this, "Erreur de scan : $errorCode", Toast.LENGTH_SHORT).show()
        onScanFailedAction(errorCode)
    }
}