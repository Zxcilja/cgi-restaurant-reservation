// app.js - обновленная версия с зонированием

document.getElementById('dateTime').value = new Date().toISOString().slice(0, 16);

let currentRecommendations = [];

window.onload = function() {
    loadTables();
    setInterval(loadTables, 10000); // every 10 seconds
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
    container.style.position = 'relative';

    const recMap = {};
    recommendations.forEach(rec => {
        if (rec.tables && rec.tables.length > 0) {
            rec.tables.forEach(t => recMap[t.id] = rec);
        } else if (rec.table) { 
            recMap[rec.table.id] = rec;
        }
    });

    tables.forEach(table => {
        const tableDiv = createTableElement(table, recMap[table.id]);
        if (table.x != null && table.y != null) {
            tableDiv.style.position = 'absolute';
            tableDiv.style.left = table.x + 'px';
            tableDiv.style.top = table.y + 'px';
        }
        container.appendChild(tableDiv);
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


    const baseSize = 140;
    const size = Math.min(180, baseSize + (table.capacity * 8));
    
    tableDiv.style.width = size + 'px';
    tableDiv.style.height = size + 'px';


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


    tableDiv.innerHTML = `
        <div class="table-info">
            <div class="table-name">${table.name}</div>
            <div class="table-capacity">${table.capacity} kohta</div>
            ${rec ? `<div class="table-score">⭐ ${Math.round(rec.score)}</div>` : ''}
            <div class="table-coords" style="font-size:10px;opacity:0.7;">(${table.x||0},${table.y||0})</div>
        </div>
    `;


    if (!tableDiv.classList.contains('occupied')) {
        tableDiv.onclick = () => openModal(table);
    }

    return tableDiv;
}

async function searchTables() {
    const guests = parseInt(document.getElementById('guests').value, 10);
    const exact = document.getElementById('exact').checked;
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

        
        currentRecommendations = currentRecommendations.filter(r => r.available);

        function totalCapacity(rec) {
            if (rec.tables && rec.tables.length > 0) {
                return rec.tables.reduce((sum, t) => sum + t.capacity, 0);
            } else if (rec.table) {
                return rec.table.capacity;
            }
            return 0;
        }

        if (exact) {
            currentRecommendations = currentRecommendations.filter(rec => totalCapacity(rec) === guests);
            if (currentRecommendations.length === 0) {
                alert('No tables with exactly ' + guests + ' seats found.');
            }
        } else {
            const hasExact = currentRecommendations.some(rec => totalCapacity(rec) === guests);
            if (hasExact) {
                currentRecommendations = currentRecommendations.filter(rec => totalCapacity(rec) === guests);
            }
        }

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