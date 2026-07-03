body {
    margin: 0;
    font-family: Arial, sans-serif;
    background: #f4f6f8;
    color: #222;
}

header {
    background: #1f4e79;
    color: white;
    padding: 28px;
    text-align: center;
}

nav {
    background: #163a5a;
    padding: 12px;
    text-align: center;
}

nav a {
    color: white;
    text-decoration: none;
    margin: 0 14px;
    font-weight: bold;
}

main {
    width: 90%;
    max-width: 1100px;
    margin: 28px auto;
}

.grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 18px;
    margin-bottom: 24px;
}

.card,
.panel {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card h2,
.panel h2 {
    color: #1f4e79;
}

.item {
    border-bottom: 1px solid #ddd;
    padding: 14px 0;
}

.item:last-child {
    border-bottom: none;
}

.green {
    color: green;
    font-weight: bold;
}

table {
    width: 100%;
    border-collapse: collapse;
}

th,
td {
    padding: 14px;
    border-bottom: 1px solid #ddd;
    text-align: left;
}

th {
    background: #e8eef3;
}

@media (max-width: 900px) {
    .grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 600px) {
    .grid {
        grid-template-columns: 1fr;
    }

    nav a {
        display: block;
        margin: 8px 0;
    }
}