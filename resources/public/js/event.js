function makeLabelOpaque(input) {
    if (input.value === '') $(input.previousSibling).fadeTo(1000, 0.5);
}

function makeLabelSolid(input) {
    if (input.value === '') $(input.previousSibling).fadeTo(1000, 1);
}

function hideLabel(input) {
    $(input.previousSibling).hide();
}