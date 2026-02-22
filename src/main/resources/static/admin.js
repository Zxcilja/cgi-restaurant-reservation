window.onload = function() {
    loadAdminTables();
    document.getElementById('saveBtn').onclick = savePositions;
};

let lastDragged = null;
let offsetX = 0;
let offsetY = 0;

async function loadAdminTables() {
    try {
        const response = await fetch('/api/tables');
        const tables = await response.json();
        renderTables(tables);
    } catch (err) {
        console.error('Failed to load tables', err);
        alert('Could not load tables');
    }
}

function renderTables(tables) {
    const container = document.getElementById('adminContainer');
    container.innerHTML = '';

    tables.forEach(tbl => {
        const div = document.createElement('div');
        div.className = 'admin-table';
        div.id = 'tab-' + tbl.id;
        div.style.width = '120px';
        div.style.height = '120px';
        div.style.left = (tbl.x || 0) + 'px';
        div.style.top = (tbl.y || 0) + 'px';
        div.textContent = tbl.name + " (" + (tbl.x||0) + "," + (tbl.y||0) + ")";
        div.dataset.id = tbl.id;
        makeDraggable(div);
        container.appendChild(div);
    });
}

function makeDraggable(el) {
    el.onmousedown = function(e) {
        lastDragged = el;
        const rect = el.getBoundingClientRect();
        offsetX = e.clientX - rect.left;
        offsetY = e.clientY - rect.top;
        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    };
    el.ondragstart = function() { return false; };
}

function onMouseMove(e) {
    if (!lastDragged) return;
    const container = document.getElementById('adminContainer').getBoundingClientRect();
    let x = e.clientX - container.left - offsetX;
    let y = e.clientY - container.top - offsetY;

    x = Math.max(0, Math.min(x, container.width - lastDragged.offsetWidth));
    y = Math.max(0, Math.min(y, container.height - lastDragged.offsetHeight));
    lastDragged.style.left = x + 'px';
    lastDragged.style.top = y + 'px';
}

function onMouseUp(e) {
    document.removeEventListener('mousemove', onMouseMove);
    document.removeEventListener('mouseup', onMouseUp);
    lastDragged = null;
}

async function savePositions() {
    const container = document.getElementById('adminContainer');
    const divs = container.querySelectorAll('.admin-table');
    const positions = [];
    divs.forEach(d => {
        const id = parseInt(d.dataset.id);
        const x = parseFloat(d.style.left);
        const y = parseFloat(d.style.top);
        positions.push({ id, x, y });
    });

    try {
        const resp = await fetch('/api/tables/positions', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(positions)
        });
        if (resp.ok) {
            alert('Layout saved');
            loadAdminTables(); 
        } else {
            const errText = await resp.text();
            alert('Error saving: ' + errText);
        }
    } catch (err) {
        console.error('Error saving positions', err);
        alert('Failed to save layout');
    }
}
