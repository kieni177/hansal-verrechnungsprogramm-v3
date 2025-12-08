export class DownloadUtils {
  static downloadFile(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  static downloadPdf(blob: Blob, id: number | string): void {
    this.downloadFile(blob, `invoice-${id}.pdf`);
  }
}
