async function readResponseBody(res) {
  const contentType = (res.headers.get('content-type') || '').toLowerCase();
  if (contentType.includes('application/json')) {
    try {
      return await res.json();
    } catch {
      // fall through
    }
  }
  return await res.text();
}

function formatBody(body) {
  if (body == null) return '';
  if (typeof body === 'string') return body;
  try {
    return JSON.stringify(body, null, 2);
  } catch {
    return String(body);
  }
}

async function toHttpError(res) {
  const body = await readResponseBody(res);
  const details = formatBody(body);
  const message = `HTTP ${res.status} ${res.statusText}${details ? `\n\n${details}` : ''}`;
  const err = new Error(message);
  err.status = res.status;
  err.body = body;
  return err;
}

async function uploadFile(file) {
  const form = new FormData();
  form.append('file', file);

  const res = await fetch('/api/files', {
    method: 'POST',
    body: form,
  });

  if (!res.ok) {
    throw await toHttpError(res);
  }

  return await readResponseBody(res);
}

function setText(el, text, isError = false) {
  if (!el) return;
  const t = (text == null ? '' : String(text));
  el.textContent = t;
  el.style.color = isError ? '#b00020' : '#0a6';
  el.style.display = t ? 'block' : 'none';
}

function filenameFromContentDisposition(headerValue) {
  if (!headerValue) return null;
  // Handles: attachment; filename="foo.txt"
  const m = /filename\*?=(?:UTF-8''|\")?([^;\"\n\r]+)\"?/i.exec(headerValue);
  if (!m) return null;
  try {
    return decodeURIComponent(m[1]);
  } catch {
    return m[1];
  }
}

async function downloadFile(id) {
  if (!id || !id.trim()) {
    throw new Error('File ID is required');
  }

  const url = `/api/files/${encodeURIComponent(id.trim())}`;
  const res = await fetch(url, { method: 'GET' });

  if (!res.ok) {
    throw await toHttpError(res);
  }

  const blob = await res.blob();
  const cd = res.headers.get('content-disposition');
  const filename = filenameFromContentDisposition(cd) || `smartvault-${id.trim()}`;

  const objUrl = URL.createObjectURL(blob);
  try {
    const a = document.createElement('a');
    a.href = objUrl;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
  } finally {
    URL.revokeObjectURL(objUrl);
  }
}

window.addEventListener('DOMContentLoaded', () => {
  const fileInput = document.getElementById('file');
  const uploadBtn = document.getElementById('uploadBtn');
  const uploadStatus = document.getElementById('uploadStatus');
  const uploadResult = document.getElementById('uploadResult');
  const uploadJson = document.getElementById('uploadJson');
  const downloadId = document.getElementById('downloadId');
  const downloadBtn = document.getElementById('downloadBtn');

  const downloadId2 = document.getElementById('downloadId2');
  const downloadBtn2 = document.getElementById('downloadBtn2');
  const downloadStatus = document.getElementById('downloadStatus');

  uploadBtn.addEventListener('click', async () => {
    uploadResult.style.display = 'none';
    setText(uploadStatus, 'Uploading...', false);

    try {
      const file = fileInput.files && fileInput.files[0];
      if (!file) {
        throw new Error('Choose a file first');
      }

      const result = await uploadFile(file);
      setText(uploadStatus, 'Upload complete.', false);

      uploadJson.textContent = JSON.stringify(result, null, 2);
      uploadResult.style.display = 'block';

      downloadId.value = result.id || '';
      downloadId2.value = result.id || '';
    } catch (e) {
      setText(uploadStatus, e && e.message ? e.message : String(e), true);
    }
  });

  downloadBtn.addEventListener('click', async () => {
    try {
      setText(downloadStatus, 'Downloading...', false);
      await downloadFile(downloadId.value);
      setText(downloadStatus, 'Download started.', false);
    } catch (e) {
      setText(downloadStatus, e && e.message ? e.message : String(e), true);
    }
  });

  downloadBtn2.addEventListener('click', async () => {
    try {
      setText(downloadStatus, 'Downloading...', false);
      await downloadFile(downloadId2.value);
      setText(downloadStatus, 'Download started.', false);
    } catch (e) {
      setText(downloadStatus, e && e.message ? e.message : String(e), true);
    }
  });
});
