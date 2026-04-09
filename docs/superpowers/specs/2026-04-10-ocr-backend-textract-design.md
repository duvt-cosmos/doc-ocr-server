---
name: OCR Backend Service with AWS Textract
date: 2026-04-10
status: Approved
---

# Design Spec: OCR Backend Service using AWS Textract

## 1. Overview
This service provides a REST API to perform Optical Character Recognition (OCR) on scanned PDF documents. It leverages AWS Textract for high-accuracy extraction and provides a synchronous "real-time" experience to the client by managing the underlying asynchronous AWS process.

## 2. Requirements
- **Input**: PDF files via REST API (`POST /ocr/extract`).
- **Output**: Plain text containing all extracted content.
- **Processing**: AWS Textract (S3-based asynchronous flow).
- **Behavior**: Synchronous response (Server blocks until processing is complete).
- **Storage**: Ephemeral S3 storage (Upload $\rightarrow$ Process $\rightarrow$ Delete).

## 3. Architecture

### 3.1 Request Lifecycle
1. **Client $\rightarrow$ Server**: `POST /ocr/extract` with PDF file.
2. **Server $\rightarrow$ S3**: Upload PDF to a temporary S3 bucket with a unique UUID key.
3. **Server $\rightarrow$ Textract**: Initiate `StartDocumentTextDetection` pointing to the S3 object.
4. **Server $\rightarrow$ Textract (Polling Loop)**:
   - Call `GetDocumentTextDetection` every 1-2 seconds.
   - If `JobStatus == SUCCEEDED`: Proceed to text aggregation.
   - If `JobStatus == FAILED`: Return `422 Unprocessable Entity`.
   - If `TIMEOUT` reached (60s): Return `504 Gateway Timeout`.
5. **Server $\rightarrow$ Textract (Aggregation)**: Fetch all pages of results and concatenate text.
6. **Server $\rightarrow$ Client**: Return aggregated plain text.
7. **Server $\rightarrow$ S3**: Delete the temporary PDF object in a `finally` block.

### 3.2 Component Breakdown

#### `OcrController`
- **Endpoint**: `POST /ocr/extract`
- **Input**: `MultipartFile`
- **Validation**: Ensure file is present and has a `.pdf` extension.

#### `OcrService`
- Orchestrates the full workflow.
- Logic for polling loop and timeout management.
- Text aggregation from paginated Textract results.

#### `AwsClientWrapper`
- Encapsulates `S3Client` and `TextractClient` (AWS SDK v2).
- Provides clean methods for:
  - `uploadDocument(InputStream is, String filename)`
  - `startOcrJob(String s3Key)`
  - `getOcrStatus(String jobId)`
  - `getResultPages(String jobId, String nextToken)`
  - `deleteDocument(String s3Key)`

## 4. Technical Details

### 4.1 Configuration
- `aws.region`: AWS region (e.g., `us-east-1`)
- `aws.s3.bucket`: Name of the temporary S3 bucket.
- `ocr.polling.interval.ms`: Time between polling calls (default: 2000ms).
- `ocr.timeout.seconds`: Maximum wait time for Textract (default: 60s).

### 4.2 Error Mapping
| Scenario | HTTP Status | Meaning |
| :--- | :--- | :--- |
| Valid Request | `200 OK` | Text extracted successfully. |
| Invalid File | `400 Bad Request` | Non-PDF or empty file. |
| Textract Failure | `422 Unprocessable Entity` | AWS could not process the document. |
| Polling Timeout | `504 Gateway Timeout` | Job took longer than the configured timeout. |
| AWS/System Error | `500 Internal Server Error` | Connectivity or internal failure. |

## 5. Testing Strategy
- **Unit Tests**: Mock `S3Client` and `TextractClient` to verify the polling loop and aggregation logic.
- **Integration Tests**: Verify the end-to-end flow using a test S3 bucket.
- **Edge Cases**:
  - Multi-page PDFs (pagination check).
  - Extremely large PDFs (timeout check).
  - Corrupted PDFs (failure status check).