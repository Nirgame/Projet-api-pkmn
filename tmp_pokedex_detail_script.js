
        function normalizeText(value) {
            return (value || '')
                .normalize('NFD')
                .replace(/[\u0300-\u036f]/g, '')
                .toLowerCase()
                .trim();
        }

        function setupAvailableCardFilters() {
            const items = Array.from(document.querySelectorAll('.available-card-item'));
            if (!items.length) {
                return;
            }

            const searchInput = document.getElementById('available-card-search');
            const setSelect = document.getElementById('available-card-set-filter');
            const raritySelect = document.getElementById('available-card-rarity-filter');
            const formSelect = document.getElementById('available-card-form-filter');
            const resetButton = document.getElementById('available-card-filter-reset');
            const visibleCount = document.getElementById('available-card-visible-count');

            const uniqueSets = [...new Map(items
                .map(item => [item.dataset.setId, item.dataset.setName])
                .filter(([setId, setName]) => setId && setName && setId.trim() !== ''))
                .entries()]
                .sort((left, right) => left[1].localeCompare(right[1], 'fr', { sensitivity: 'base' }));
            const uniqueForms = [...new Set(items
                .map(item => item.dataset.form)
                .filter(value => value && value.trim() !== ''))]
                .sort((left, right) => left.localeCompare(right, 'fr', { sensitivity: 'base' }));
            const uniqueRarities = [...new Set(items
                .map(item => item.dataset.rarity)
                .filter(value => value && value.trim() !== ''))]
                .sort((left, right) => left.localeCompare(right, 'fr', { sensitivity: 'base' }));

            uniqueSets.forEach(([setId, setName]) => {
                const option = document.createElement('option');
                option.value = setId;
                option.textContent = setName;
                setSelect.appendChild(option);
            });

            uniqueForms.forEach(formName => {
                const option = document.createElement('option');
                option.value = formName;
                option.textContent = formName;
                formSelect.appendChild(option);
            });

            uniqueRarities.forEach(rarityName => {
                const option = document.createElement('option');
                option.value = rarityName;
                option.textContent = rarityName;
                raritySelect.appendChild(option);
            });

            function applyFilters() {
                const searchValue = normalizeText(searchInput.value);
                const setValue = normalizeText(setSelect.value);
                const rarityValue = normalizeText(raritySelect.value);
                const formValue = normalizeText(formSelect.value);
                let visible = 0;

                items.forEach(item => {
                    const haystack = normalizeText([
                        item.dataset.name,
                        item.dataset.secondary,
                        item.dataset.cardId,
                        item.dataset.setId,
                        item.dataset.setName,
                        item.dataset.rarity,
                        item.dataset.form
                    ].join(' '));

                    const matchesSearch = !searchValue || haystack.includes(searchValue);
                    const matchesSet = !setValue || normalizeText(item.dataset.setId) === setValue;
                    const matchesRarity = !rarityValue || normalizeText(item.dataset.rarity) === rarityValue;
                    const matchesForm = !formValue || normalizeText(item.dataset.form) === formValue;
                    const shouldShow = matchesSearch && matchesSet && matchesRarity && matchesForm;

                    item.style.display = shouldShow ? '' : 'none';
                    if (shouldShow) {
                        visible += 1;
                    }
                });

                visibleCount.textContent = String(visible);
            }

            searchInput.addEventListener('input', applyFilters);
            setSelect.addEventListener('change', applyFilters);
            raritySelect.addEventListener('change', applyFilters);
            formSelect.addEventListener('change', applyFilters);
            resetButton.addEventListener('click', () => {
                searchInput.value = '';
                setSelect.value = '';
                raritySelect.value = '';
                formSelect.value = '';
                applyFilters();
            });

            applyFilters();
        }

        function addToCollection(cardId) {
            fetch('/api/collection/cards/' + cardId, withCsrf({
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'same-origin'
            }))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const quantity = data.card && typeof data.card.quantity === 'number' ? data.card.quantity : 1;
                    window.dispatchEvent(new CustomEvent('collection-card-updated', {
                        detail: {
                            cardId: cardId,
                            quantity: quantity
                        }
                    }));
                    return;
                }
                alert('Erreur: ' + data.error);
            })
            .catch(() => alert('Erreur lors de l ajout a la collection'));
        }

        function removeFromCollection(cardId) {
            fetch('/api/collection/cards/' + cardId, withCsrf({
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'same-origin'
            }))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const quantity = typeof data.quantity === 'number' ? data.quantity : 0;
                    window.dispatchEvent(new CustomEvent('collection-card-updated', {
                        detail: {
                            cardId: cardId,
                            quantity: quantity
                        }
                    }));
                    return;
                }
                alert('Erreur lors de la suppression');
            })
            .catch(() => alert('Erreur lors de la suppression de la collection'));
        }

        function getAssignmentComment() {
            return document.getElementById('assignmentComment')?.value || '';
        }

        window.getPokedexAssignmentComment = getAssignmentComment;

        function updateAssignmentCommentActionLabel() {
            const label = document.getElementById('assignmentCommentActionLabel');
            if (!label) {
                return;
            }

            const hasComment = getAssignmentComment().trim().length > 0;
            label.textContent = hasComment ? 'Supprimer' : 'Ajouter';
        }

        function escapeAssignmentHtml(value) {
            return String(value || '')
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;');
        }

        function bindPokedexAssignmentZoom() {
            initializeCardZoom('#pokedex-assignment-state .zoom-hover-image');
            const assignmentImage = document.querySelector('#pokedex-assignment-state .zoom-hover-image');
            if (!assignmentImage) {
                return;
            }

            assignmentImage.style.cursor = 'zoom-in';
            if (assignmentImage.dataset.assignmentZoomBound === 'true') {
                return;
            }

            assignmentImage.dataset.assignmentZoomBound = 'true';
            assignmentImage.addEventListener('click', () => {
                initializeCardZoom('#pokedex-assignment-state .zoom-hover-image');
            });
        }

        function openPokedexZoom(element, selector) {
            if (!element) {
                return;
            }

            if (typeof window.openCardZoomForElement === 'function') {
                window.openCardZoomForElement(selector, element);
                return;
            }

            initializeCardZoom(selector);
            element.click();
        }

        function renderPokedexAssignmentState(cardData) {
            const container = document.getElementById('pokedex-assignment-state');
            if (!container) {
                return;
            }

            const pokemonId = container.dataset.pokemonId || '';

            if (!cardData) {
                const missingChecked = container.dataset.missingMarked === 'true';
                container.innerHTML = '' +
                    '<div class="text-center py-4">' +
                    (missingChecked
                        ? '<div class="display-4 text-warning mb-2"><i class="fas fa-ban"></i></div><p class="text-warning-emphasis fw-semibold mb-3">Aucune carte correspondante</p>'
                        : '<div class="display-4 text-dark mb-2"><i class="fas fa-circle-dot"></i></div><p class="text-muted mb-3">Aucune carte n\'est encore assignee a ce Pokemon.</p>') +
                    '<div class="form-check text-start border rounded p-3 ' + (missingChecked ? 'bg-warning-subtle border-warning' : 'bg-light') + '">' +
                    '<input id="missingCardCheckbox" class="form-check-input" type="checkbox" data-pokemon-id="' + escapeAssignmentHtml(pokemonId) + '" ' + (missingChecked ? 'checked' : '') + ' onchange="toggleMissingCard(this.dataset.pokemonId, this.checked)">' +
                    '<label class="form-check-label" for="missingCardCheckbox">Aucune carte correspondante</label>' +
                    '</div>' +
                    '</div>';
                return;
            }

            container.dataset.missingMarked = 'false';
            const quantity = Number.parseInt(cardData.zoomOwnedCount || '0', 10) || 0;
            const quantityLabel = quantity > 0 ? 'x' + quantity : '';

            container.innerHTML = '' +
                '<img class="img-fluid mb-3 zoom-hover-image" src="' + escapeAssignmentHtml(cardData.zoomSrc) + '" alt="' + escapeAssignmentHtml(cardData.zoomTitle) + '"' +
                ' data-zoom-src="' + escapeAssignmentHtml(cardData.zoomSrc) + '"' +
                ' data-zoom-title="' + escapeAssignmentHtml(cardData.zoomTitle) + '"' +
                ' data-zoom-subtitle="' + escapeAssignmentHtml(cardData.zoomSubtitle) + '"' +
                ' data-zoom-card-id="' + escapeAssignmentHtml(cardData.zoomCardId) + '"' +
                ' data-zoom-local-id="' + escapeAssignmentHtml(cardData.zoomLocalId) + '"' +
                ' data-zoom-set-name="' + escapeAssignmentHtml(cardData.zoomSetName) + '"' +
                ' data-zoom-quantity="' + escapeAssignmentHtml(quantityLabel) + '"' +
                ' data-zoom-variant="' + escapeAssignmentHtml(cardData.zoomVariant) + '"' +
                ' data-zoom-owned-count="' + escapeAssignmentHtml(String(quantity)) + '"' +
                ' data-zoom-can-add="true" data-zoom-can-remove="' + escapeAssignmentHtml(String(quantity > 0)) + '"' +
                ' data-zoom-clear-assignment-pokemon-id="' + escapeAssignmentHtml(pokemonId) + '"' +
                ' onclick="openPokedexZoom(this, \'#pokedex-assignment-state .zoom-hover-image\')"' +
                ' onerror="this.onerror=null;this.src=\'/images/placeholder.svg\'">' +
                '<h4 class="h6 mb-1">' + escapeAssignmentHtml(cardData.zoomTitle) + '</h4>' +
                (cardData.zoomSubtitle ? '<div class="text-muted small mb-2">' + escapeAssignmentHtml(cardData.zoomSubtitle) + '</div>' : '') +
                (cardData.zoomSetName ? '<div class="small text-muted mb-1">' + escapeAssignmentHtml(cardData.zoomSetName) + '</div>' : '') +
                '<p class="small text-muted mb-3">' + escapeAssignmentHtml(cardData.zoomCardId) + '</p>' +
                '<button class="btn btn-outline-danger w-100" data-pokemon-id="' + escapeAssignmentHtml(pokemonId) + '" onclick="clearAssignment(this.dataset.pokemonId)">' +
                '<i class="fas fa-trash"></i> Retirer l\'assignation' +
                '</button>';

            bindPokedexAssignmentZoom();
        }

        function assignCard(pokemonId, cardId) {
            fetch('/api/pokedex/pokemon/' + pokemonId + '/assign/' + cardId, withCsrf({
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    comment: getAssignmentComment()
                }),
                credentials: 'same-origin'
            }))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const cardElement = document.querySelector('[data-zoom-card-id="' + CSS.escape(cardId) + '"]');
                    const imageDataset = cardElement ? { ...cardElement.dataset } : {
                        zoomCardId: cardId,
                        zoomTitle: data.cardName || '',
                        zoomSubtitle: '',
                        zoomSrc: '',
                        zoomSetName: '',
                        zoomVariant: '',
                        zoomOwnedCount: '0'
                    };
                    imageDataset.zoomClearAssignmentPokemonId = String(pokemonId);
                    window.dispatchEvent(new CustomEvent('pokedex-assignment-updated', {
                        detail: {
                            pokemonId: pokemonId,
                            cardId: cardId,
                            comment: data.comment || getAssignmentComment(),
                            imageDataset: imageDataset
                        }
                    }));
                    return;
                }
                alert('Erreur: ' + data.error);
            })
            .catch(() => alert('Erreur lors de l assignation'));
        }

        function clearAssignment(pokemonId) {
            fetch('/api/pokedex/pokemon/' + pokemonId + '/assignment', withCsrf({
                method: 'DELETE',
                credentials: 'same-origin'
            }))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    window.dispatchEvent(new CustomEvent('pokedex-assignment-cleared', {
                        detail: {
                            pokemonId: pokemonId
                        }
                    }));
                    return;
                }
                alert('Erreur: ' + data.error);
            })
            .catch(() => alert('Erreur lors de la suppression de l assignation'));
        }

        function toggleMissingCard(pokemonId, checked) {
            const method = checked ? 'POST' : 'DELETE';
            const url = checked
                ? '/api/pokedex/pokemon/' + pokemonId + '/mark-missing'
                : '/api/pokedex/pokemon/' + pokemonId + '/assignment';

            fetch(url, withCsrf({
                method: method,
                headers: checked ? {
                    'Content-Type': 'application/json'
                } : undefined,
                body: checked ? JSON.stringify({
                    comment: getAssignmentComment()
                }) : undefined,
                credentials: 'same-origin'
            }))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    const container = document.getElementById('pokedex-assignment-state');
                    if (container) {
                        container.dataset.missingMarked = checked ? 'true' : 'false';
                    }
                    renderPokedexAssignmentState(null);
                    return;
                }
                alert('Erreur: ' + data.error);
            })
            .catch(() => alert('Erreur lors de la mise a jour du statut de carte'));
        }

        function savePokemonComment(pokemonId) {
            fetch('/api/pokedex/pokemon/' + pokemonId + '/comment', withCsrf({
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    comment: getAssignmentComment()
                }),
                credentials: 'same-origin'
            }))
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    updateAssignmentCommentActionLabel();
                    return;
                }
                alert('Erreur: ' + data.error);
            })
            .catch(() => alert('Erreur lors de l enregistrement du commentaire'));
        }

        document.addEventListener('DOMContentLoaded', function() {
            setupAvailableCardFilters();
            initializeCardZoom('.zoom-hover-image');
            bindPokedexAssignmentZoom();
            updateAssignmentCommentActionLabel();

            const assignmentCommentField = document.getElementById('assignmentComment');
            if (assignmentCommentField) {
                assignmentCommentField.addEventListener('input', updateAssignmentCommentActionLabel);
            }

            window.addEventListener('pokedex-assignment-updated', event => {
                const detail = event.detail || {};
                const assignmentContainer = document.getElementById('pokedex-assignment-state');
                if (!assignmentContainer || assignmentContainer.dataset.pokemonId !== String(detail.pokemonId || '')) {
                    return;
                }

                renderPokedexAssignmentState(detail.imageDataset || null);
            });

            window.addEventListener('pokedex-assignment-cleared', event => {
                const detail = event.detail || {};
                const assignmentContainer = document.getElementById('pokedex-assignment-state');
                if (!assignmentContainer || assignmentContainer.dataset.pokemonId !== String(detail.pokemonId || '')) {
                    return;
                }

                renderPokedexAssignmentState(null);
            });

            window.addEventListener('collection-card-updated', event => {
                const detail = event.detail || {};
                const assignmentContainer = document.getElementById('pokedex-assignment-state');
                if (!assignmentContainer) {
                    return;
                }

                const assignedImage = assignmentContainer.querySelector('[data-zoom-card-id]');
                if (!assignedImage || assignedImage.dataset.zoomCardId !== detail.cardId) {
                    return;
                }

                assignedImage.dataset.zoomOwnedCount = String(detail.quantity || 0);
                assignedImage.dataset.zoomQuantity = detail.quantity > 0 ? 'x' + detail.quantity : '';
                assignedImage.dataset.zoomCanRemove = String((detail.quantity || 0) > 0);
                renderPokedexAssignmentState({ ...assignedImage.dataset });
            });
        });
    