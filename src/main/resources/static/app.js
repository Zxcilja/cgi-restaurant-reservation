// app.js - обновленная версия с зонированием

document.getElementById('dateTime').value = new Date().toISOString().slice(0, 16);

let currentRecommendations = [];

window.onload = function() {
    loadTables();
};

async function loadTables() {
    try {
        const response = await fetch('/api/tables');
        const tables = await response.json();
        displayTables(tables, []);
    } catch (error) {
        console.error('Error loading tables:', error);
    }
}

function displayTables(tables, recommendations) {
    const container = document.getElementById('tablesContainer');
    container.innerHTML = '';

    // сопоставление таблицы -> рекомендация (любая таблица из пары ссылается на общую рекомендацию)
    const recMap = {};
    recommendations.forEach(rec => {
        if (rec.tables && rec.tables.length > 0) {
            rec.tables.forEach(t => recMap[t.id] = rec);
        } else if (rec.table) { // на случай старых объектов
            recMap[rec.table.id] = rec;
        }
    });

    // Группировка по зонам
    const zones = {};
    tables.forEach(table => {
        if (!zones[table.zone]) {
            zones[table.zone] = [];
        }
        zones[table.zone].push(table);
    });

    // Сортировка столов внутри зоны
    Object.keys(zones).forEach(zone => {
        zones[zone].sort((a, b) => a.name.localeCompare(b.name));
    });

    // Порядок отображения зон
    const zoneOrder = ['Main Hall', 'VIP Hall', 'Terrace'];
    const sortedZones = Object.keys(zones).sort((a, b) => {
        const indexA = zoneOrder.indexOf(a);
        const indexB = zoneOrder.indexOf(b);
        if (indexA === -1 && indexB === -1) return a.localeCompare(b);
        if (indexA === -1) return 1;
        if (indexB === -1) return -1;
        return indexA - indexB;
    });

    // Отображение по зонам
    sortedZones.forEach(zoneName => {
        const zoneSection = document.createElement('div');
        zoneSection.className = 'zone-section';

        const zoneHeader = document.createElement('div');
        zoneHeader.className = 'zone-header';
        zoneHeader.innerHTML = `<h4>${getZoneIcon(zoneName)} ${zoneName}</h4>`;
        zoneSection.appendChild(zoneHeader);

        const zoneGrid = document.createElement('div');
        zoneGrid.className = 'zone-grid';

        zones[zoneName].forEach(table => {
            const tableDiv = createTableElement(table, recMap[table.id]);
            zoneGrid.appendChild(tableDiv);
        });

        zoneSection.appendChild(zoneGrid);
        container.appendChild(zoneSection);
    });
}

function getZoneIcon(zoneName) {
    const icons = {
        'Main Hall': '🏛️',
        'VIP Hall': '👑',
        'Terrace': '🌿'
    };
    return icons[zoneName] || '📍';
}

function createTableElement(table, rec) {
    const tableDiv = document.createElement('div');
    tableDiv.className = 'table';

    // Фиксированные размеры вместо динамических
    const baseSize = 140;
    const size = Math.min(180, baseSize + (table.capacity * 8));
    
    tableDiv.style.width = size + 'px';
    tableDiv.style.height = size + 'px';

    // Определение статуса
    if (rec) {
        if (rec.score > 80) {
            tableDiv.classList.add('recommended');
        } else if (rec.available) {
            tableDiv.classList.add('available');
        } else {
            tableDiv.classList.add('occupied');
        }
    } else {
        tableDiv.classList.add('available');
    }

    // Контент
    tableDiv.innerHTML = `
        <div class="table-info">
            <div class="table-name">${table.name}</div>
            <div class="table-capacity">${table.capacity} kohta</div>
            ${rec ? `<div class="table-score">⭐ ${Math.round(rec.score)}</div>` : ''}
        </div>
    `;

    // Клик только для доступных/рекомендованных
    if (!tableDiv.classList.contains('occupied')) {
        tableDiv.onclick = () => openModal(table);
    }

    return tableDiv;
}

async function searchTables() {
    const guests = document.getElementById('guests').value;
    const dateTime = document.getElementById('dateTime').value;
    const zone = document.getElementById('zone').value;
    const window = document.getElementById('window').checked;
    const privateArea = document.getElementById('private').checked;
    const accessible = document.getElementById('accessible').checked;

    if (!dateTime) {
        alert('Please select a date and time!');
        return;
    }

    try {
        const url = `/api/tables/recommendations?guests=${guests}&time=${dateTime}` +
            `${zone ? '&zone=' + encodeURIComponent(zone) : ''}` +
            `&window=${window}&privateArea=${privateArea}&accessible=${accessible}`;

        const response = await fetch(url);
        currentRecommendations = await response.json();

        
        const filteredTables = currentRecommendations.flatMap(rec => {
            if (rec.tables && rec.tables.length > 0) {
                return rec.tables;
            } else if (rec.table) {
                return [rec.table];
            }
            return [];
        });
        displayTables(filteredTables, currentRecommendations);
    } catch (error) {
        console.error('Error searching tables:', error);
        alert('Error during search!');
    }
}

function openModal(table) {
    document.getElementById('modalTableName').value = table.name;
    document.getElementById('modalTableId').value = table.id;
    document.getElementById('modalGuests').value = document.getElementById('guests').value;
    document.getElementById('modalTime').value = document.getElementById('dateTime').value;
    document.getElementById('bookingModal').classList.add('active');
}

function closeModal() {
    document.getElementById('bookingModal').classList.remove('active');
}

document.getElementById('bookingForm').onsubmit = async function(e) {
    e.preventDefault();

    const reservation = {
        tableIds: [parseInt(document.getElementById('modalTableId').value)],
        customerName: document.getElementById('customerName').value,
        customerEmail: document.getElementById('customerEmail').value,
        numberOfGuests: parseInt(document.getElementById('modalGuests').value),
        startTime: document.getElementById('modalTime').value
    };

    try {
        const response = await fetch('/api/reservations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(reservation)
        });

        if (response.ok) {
            alert('Reservation created successfully!');
            closeModal();
            searchTables();
        } else {
            const error = await response.text();
            alert('Error: ' + error);
        }
    } catch (error) {
        console.error('Error creating reservation:', error);
        alert('Error during reservation!');
    }
};