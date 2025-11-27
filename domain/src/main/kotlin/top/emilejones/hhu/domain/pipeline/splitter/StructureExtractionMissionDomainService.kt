package top.emilejones.hhu.domain.pipeline.splitter

import top.emilejones.hhu.domain.pipeline.infrastructure.gateway.StructureExtractionGateway
import top.emilejones.hhu.domain.pipeline.ocr.OcrMission
import top.emilejones.hhu.domain.pipeline.ocr.OcrMissionResult

class StructureExtractionMissionDomainService(
    val structureExtractionGateway: StructureExtractionGateway
) {

    fun startStructureExtraction(
        ocrMission: OcrMission,
        structureExtractionMission: StructureExtractionMission
    ) {
        if (!ocrMission.isSuccess()) {
            structureExtractionMission.failure("OCR任务没有成功，无法开启文本结构提取任务")
            return
        }

        require(ocrMission.result is OcrMissionResult.Success) {
            "代码逻辑异常，成功执行后的OCR任务没有产生结果"
        }

        val processedDocument = (ocrMission.result as OcrMissionResult.Success).processedDocument

        structureExtractionMission.processedDocumentId = processedDocument.id

        structureExtractionMission.start()

        val fileNodeResult = kotlin.runCatching {
            structureExtractionGateway.extract(structureExtractionMission.processedDocumentId!!)
        }

       if (fileNodeResult.isFailure){
           val msg = fileNodeResult.exceptionOrNull()?.message ?: "未知的错误"
           structureExtractionMission.failure(msg)
       }
        val fileNode = fileNodeResult.getOrThrow()
        structureExtractionMission.success(fileNode.elementId, fileNode.childNodeNumber)
    }
}