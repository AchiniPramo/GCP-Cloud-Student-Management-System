// ── API ──────────────────────────────────────────────────────
const API_BASE_URL = 'http://localhost:8080/api/v1/employees';

// ── DOM Refs ─────────────────────────────────────────────────
const studentForm        = document.getElementById('studentForm');
const tableBody          = document.getElementById('tableBody');
const searchInput        = document.getElementById('searchInput');
const refreshBtn         = document.getElementById('refreshBtn');
const studentNameInput   = document.getElementById('studentName');
const studentAddressInput= document.getElementById('studentAddress');
const studentContactInput= document.getElementById('studentContact');
const studentImageInput  = document.getElementById('studentImage');
const imagePreview       = document.getElementById('imagePreview');
const submitBtn          = document.getElementById('submitBtn');
const resetBtn           = document.getElementById('resetBtn');
const totalCountEl       = document.getElementById('totalCount');

// Edit Modal
const editModal      = document.getElementById('editModal');
const editForm       = document.getElementById('editForm');
const editIdInput    = document.getElementById('editId');
const editNameInput  = document.getElementById('editName');
const editAddressInput  = document.getElementById('editAddress');
const editContactInput  = document.getElementById('editContact');
const editImageInput    = document.getElementById('editImage');
const editImagePreview  = document.getElementById('editImagePreview');
const editMessage       = document.getElementById('editMessage');
const closeModalBtn     = document.getElementById('closeModalBtn');
const cancelEditBtn     = document.getElementById('cancelEditBtn');

// Delete Modal
const deleteModal      = document.getElementById('deleteModal');
const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
const cancelDeleteBtn  = document.getElementById('cancelDeleteBtn');
let deleteStudentId    = null;

// Loading
const loadingSpinner = document.getElementById('loadingSpinner');

// Data
let allStudents = [];

// ── Init ──────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    loadStudents();
    setupEventListeners();
});

// ── Event Listeners ───────────────────────────────────────────
function setupEventListeners() {
    studentForm.addEventListener('submit', handleAddStudent);
    resetBtn.addEventListener('click', () => {
        studentForm.reset();
        resetPreview(imagePreview);
    });

    studentImageInput.addEventListener('change', e => previewImage(e, imagePreview));
    editImageInput.addEventListener('change',    e => previewImage(e, editImagePreview));

    searchInput.addEventListener('input', handleSearch);
    refreshBtn.addEventListener('click', () => {
        refreshBtn.querySelector('i').classList.add('fa-spin');
        loadStudents().finally(() => refreshBtn.querySelector('i').classList.remove('fa-spin'));
    });

    editForm.addEventListener('submit', handleUpdateStudent);
    closeModalBtn.addEventListener('click', closeEditModal);
    cancelEditBtn.addEventListener('click', closeEditModal);

    confirmDeleteBtn.addEventListener('click', handleConfirmDelete);
    cancelDeleteBtn.addEventListener('click',  closeDeleteModal);

    window.addEventListener('click', e => {
        if (e.target === editModal)   closeEditModal();
        if (e.target === deleteModal) closeDeleteModal();
    });
}

// ── Load Students ─────────────────────────────────────────────
async function loadStudents() {
    try {
        showSpinner();
        const res = await fetch(API_BASE_URL);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        allStudents = Array.isArray(data) ? data : (data._embedded?.employees || []);
        renderTable(allStudents);
        totalCountEl.textContent = allStudents.length;
    } catch (err) {
        console.error(err);
        showToast('Failed to load students. Is the backend running?', 'error');
        renderTable([]);
        totalCountEl.textContent = 0;
    } finally {
        hideSpinner();
    }
}

// ── Add Student ───────────────────────────────────────────────
async function handleAddStudent(e) {
    e.preventDefault();

    const name    = studentNameInput.value.trim();
    const address = studentAddressInput.value.trim();
    const contact = studentContactInput.value.trim();

    if (!name || !address || !contact) {
        showToast('All fields are required!', 'error'); return;
    }
    if (contact.length < 10 || !/^\d+$/.test(contact)) {
        showToast('Enter a valid contact number (digits only, ≥ 10 digits)', 'error'); return;
    }

    const formData = new FormData();
    formData.append('name', name);
    formData.append('address', address);
    formData.append('contact', contact);
    if (studentImageInput.files.length) formData.append('image', studentImageInput.files[0]);

    try {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Adding…';
        const res = await fetch(API_BASE_URL, { method: 'POST', body: formData });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.message || `HTTP ${res.status}`);
        }
        showToast('Student added successfully! 🎉', 'success');
        studentForm.reset();
        resetPreview(imagePreview);
        loadStudents();
    } catch (err) {
        showToast(`Failed to add student: ${err.message}`, 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-plus-circle"></i> Add Student';
    }
}

// ── Render Table ──────────────────────────────────────────────
function renderTable(students) {
    if (!students.length) {
        tableBody.innerHTML = `
          <tr class="no-data">
            <td colspan="6">
              <div class="empty-state">
                <i class="fas fa-users-slash"></i>
                <span>No students found</span>
              </div>
            </td>
          </tr>`;
        return;
    }

    tableBody.innerHTML = students.map(s => `
      <tr>
        <td><span class="id-badge">${s.id}</span></td>
        <td>
          ${s.imageUrl && isValidUrl(s.imageUrl)
            ? `<img src="${s.imageUrl}" alt="${esc(s.name)}" class="student-photo"
                    onerror="this.replaceWith(noPhoto())">`
            : `<div class="student-photo-placeholder"><i class="fas fa-user"></i></div>`}
        </td>
        <td><strong>${esc(s.name)}</strong></td>
        <td>${esc(s.address)}</td>
        <td>${esc(s.contact)}</td>
        <td>
          <div class="action-buttons">
            <button class="btn-edit"   onclick="openEditModal(${s.id})">
              <i class="fas fa-pen"></i> Edit
            </button>
            <button class="btn-delete" onclick="openDeleteModal(${s.id})">
              <i class="fas fa-trash"></i> Delete
            </button>
          </div>
        </td>
      </tr>`).join('');
}

function noPhoto() {
    const d = document.createElement('div');
    d.className = 'student-photo-placeholder';
    d.innerHTML = '<i class="fas fa-user"></i>';
    return d;
}

// ── Edit Modal ────────────────────────────────────────────────
function openEditModal(id) {
    const s = allStudents.find(x => x.id === id);
    if (!s) return;

    editIdInput.value      = s.id;
    editNameInput.value    = s.name;
    editAddressInput.value = s.address;
    editContactInput.value = s.contact;
    editMessage.textContent = '';
    editMessage.className   = 'message';

    if (s.imageUrl && isValidUrl(s.imageUrl)) {
        editImagePreview.innerHTML =
            `<img src="${s.imageUrl}" style="width:100%;height:100%;object-fit:cover;" onerror="this.style.display='none'">`;
    } else {
        resetPreview(editImagePreview);
    }

    editModal.classList.add('open');
}

function closeEditModal() {
    editModal.classList.remove('open');
    editForm.reset();
    resetPreview(editImagePreview);
}

// ── Update Student ────────────────────────────────────────────
async function handleUpdateStudent(e) {
    e.preventDefault();

    const name    = editNameInput.value.trim();
    const address = editAddressInput.value.trim();
    const contact = editContactInput.value.trim();
    const id      = editIdInput.value;

    if (!name || !address || !contact) {
        showError('All fields are required!', editMessage); return;
    }
    if (contact.length < 10 || !/^\d+$/.test(contact)) {
        showError('Enter a valid contact number', editMessage); return;
    }

    const formData = new FormData();
    formData.append('name', name);
    formData.append('address', address);
    formData.append('contact', contact);
    if (editImageInput.files.length) formData.append('image', editImageInput.files[0]);

    try {
        const res = await fetch(`${API_BASE_URL}/${id}`, { method: 'PUT', body: formData });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || `HTTP ${res.status}`);
        }
        showToast('Student updated successfully! ✅', 'success');
        closeEditModal();
        loadStudents();
    } catch (err) {
        showError(`Failed to update: ${err.message}`, editMessage);
    }
}

// ── Delete Modal ──────────────────────────────────────────────
function openDeleteModal(id) {
    deleteStudentId = id;
    deleteModal.classList.add('open');
}

function closeDeleteModal() {
    deleteModal.classList.remove('open');
    deleteStudentId = null;
}

async function handleConfirmDelete() {
    if (!deleteStudentId) return;
    try {
        const res = await fetch(`${API_BASE_URL}/${deleteStudentId}`, { method: 'DELETE' });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        showToast('Student deleted successfully.', 'info');
        closeDeleteModal();
        loadStudents();
    } catch (err) {
        showToast(`Failed to delete: ${err.message}`, 'error');
        closeDeleteModal();
    }
}

// ── Search ────────────────────────────────────────────────────
function handleSearch() {
    const q = searchInput.value.toLowerCase();
    renderTable(q
        ? allStudents.filter(s =>
            s.name.toLowerCase().includes(q) ||
            s.contact.toLowerCase().includes(q) ||
            s.address.toLowerCase().includes(q))
        : allStudents
    );
}

// ── Toast Notifications ───────────────────────────────────────
function showToast(message, type = 'default') {
    const container = document.getElementById('toastContainer');
    const icons = {
        success: 'fas fa-check',
        error:   'fas fa-times',
        info:    'fas fa-info',
        default: 'fas fa-bell',
    };
    const t = document.createElement('div');
    t.className = `toast ${type}`;
    t.innerHTML = `
      <span class="toast-icon"><i class="${icons[type] || icons.default}"></i></span>
      <span>${message}</span>`;
    container.appendChild(t);
    setTimeout(() => {
        t.classList.add('hide');
        t.addEventListener('animationend', () => t.remove());
    }, 3500);
}

// ── Inline Message (edit form only) ───────────────────────────
function showError(msg, el) {
    el.textContent = msg;
    el.className   = 'message error';
    setTimeout(() => { el.className = 'message'; }, 4000);
}

// ── Spinner ───────────────────────────────────────────────────
function showSpinner() { loadingSpinner.style.display = 'flex'; }
function hideSpinner() { loadingSpinner.style.display = 'none'; }

// ── Helpers ───────────────────────────────────────────────────
function esc(text) {
    return String(text).replace(/[&<>"']/g,
        c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c]));
}

function isValidUrl(url) {
    if (!url || typeof url !== 'string') {
        return false;
    }

    // Support absolute URLs and app-relative URLs.
    if (url.startsWith('/')) {
        return true;
    }

    try {
        const parsed = new URL(url);
        return parsed.protocol === 'http:' || parsed.protocol === 'https:';
    } catch {
        return false;
    }
}

function previewImage(e, previewEl) {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
        previewEl.innerHTML =
            `<img src="${reader.result}" style="width:100%;height:100%;object-fit:cover;" alt="Preview">`;
    };
    reader.readAsDataURL(file);
}

function resetPreview(previewEl) {
    previewEl.innerHTML = `
      <div class="photo-placeholder">
        <i class="fas fa-camera"></i><span>Upload Photo</span>
      </div>`;
}
